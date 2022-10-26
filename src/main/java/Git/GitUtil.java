package Git;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import java.util.*;

public class GitUtil {

    /**
     *  列出特定仓库中所有Commit
     */
    public static List<RevCommit>getCommits(Repository repository){
        List<RevCommit>result = new ArrayList<>();
        try{
            Git git = new Git(repository);
            Iterator<RevCommit>commits = git.log().call().iterator();
            while(commits.hasNext()){
                RevCommit commit = commits.next();
                result.add(commit);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     *  列出特定仓库某个Commit修改的所有java文件
     */
    public static Set<String> getAllFileNamesModifiedByCommit(Repository repository, String commitId){
        Set<String > result = new HashSet<>();
        try {
            RevWalk rw = new RevWalk(repository);
            ObjectId curId = repository.resolve(commitId);
            RevCommit cur = rw.parseCommit(curId);
            RevCommit par = rw.parseCommit(cur.getParent(0).getId());
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            List<DiffEntry> diffs = df.scan(par.getTree(), cur.getTree());

            for(DiffEntry diff: diffs){
                String fileName = diff.getNewPath();
                if(fileName.endsWith(".java")){
                    result.add(fileName);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     *  是否是测试文件
     */
    public static boolean isTestFile(String filename){
        String name = filename.substring(filename.lastIndexOf('/')+1,filename.lastIndexOf("."));
        if(name.toLowerCase().startsWith("test")||name.toLowerCase().endsWith("test")){
            return true;
        }
        return false;
    }

    /**
     *  是否包含测试文件
     */
    public static boolean hasTestFile(Set<String>fileNames){
        for(String f:fileNames){
            if(isTestFile(f))
                return true;
        }
        return false;
    }

    /**
     *  列出包含测试文件的Commit
     */
    public static List<RevCommit>getCommitsContainsTestCode(Repository repository, List<RevCommit>commits){
        List<RevCommit>result = new ArrayList<>();
        for(RevCommit commit:commits){
            Set<String>fileNames = getAllFileNamesModifiedByCommit(repository,commit.getId().getName());
            if(hasTestFile(fileNames)){
                result.add(commit);
            }
        }
        return result;
    }

    /**
     * 列出一个Commit所有的修改编辑操作
     * @return map: 文件名->该文件修改的编辑操作列表
     */
    public static HashMap<String,List<MyEdit>>getEdits(Repository repository,String commitId){
        HashMap<String,List<MyEdit>> result = new HashMap<>();
        try {
            RevWalk rw = new RevWalk(repository);
            ObjectId curId = repository.resolve(commitId);
            RevCommit cur = rw.parseCommit(curId);
            RevCommit par = rw.parseCommit(cur.getParent(0).getId());
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            List<DiffEntry> diffs = df.scan(par.getTree(), cur.getTree());

            for(DiffEntry diff: diffs){
                String fileName = diff.getNewPath();
                if(!fileName.endsWith(".java"))continue;
                List<MyEdit> myEdits = new ArrayList<>();
                try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                    formatter.setRepository(repository);
                    //formatter.format(diff);
                    FileHeader fileheader = formatter.toFileHeader(diff);
                    List<HunkHeader>hunks = (List<HunkHeader>)fileheader.getHunks();
                    for(HunkHeader hunkHeader:hunks){
                        EditList edits = hunkHeader.toEditList();
                        for(Edit edit:edits){
                            //System.out.println(edit.toString());
                            myEdits.add(new MyEdit(fileName,edit.getBeginB(),edit.getEndB()));
                        }
                    }
                    result.put(fileName,myEdits);
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    /**
     *  返回某个Commit后某个文件的内容
     */
    public static String getFileFromCommit(Repository repository, ObjectId commitId, String filePath){
        String result = "";
        try(TreeWalk treeWalk = new TreeWalk(repository)){
            treeWalk.reset(repository.resolve(commitId.getName()+"^{tree}"));
            treeWalk.setFilter(PathFilter.create(filePath));
            treeWalk.setRecursive(true);
            if(treeWalk.next()){
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                result = new String(loader.getBytes());
            }
        }catch (Exception e){
            System.out.println(" error + " + commitId.getName() + "\n");
            System.out.println(e.getMessage());
        }
        return result;
    }

    /**
     *  返回一个Commit修改的所有文件中的方法
     */
    public static HashMap<String,List<ChangedMethod>>getMethods(Repository repository,String commitId){
        HashMap<String,List<ChangedMethod>> result = new HashMap<>();
        try{
            ObjectId curId = repository.resolve(commitId);
            Set<String>fs = getAllFileNamesModifiedByCommit(repository,commitId);
            for(String f:fs){
                String content = getFileFromCommit(repository,curId,f);
                ASTParser parser = ASTParser.newParser(AST.JLS10);
                parser.setSource(content.toCharArray());
                parser.setKind(ASTParser.K_COMPILATION_UNIT);
                parser.setResolveBindings(true);
                parser.setBindingsRecovery(true);

                parser.setEnvironment(null, new String[]{"D:\\Code\\lucene"}, new String[]{"utf-8"}, true);

                Map<String, String> options = JavaCore.getOptions();
                options.put("org.eclipse.jdt.core.compiler.source", "1.8");
                parser.setCompilerOptions(options);

                //System.out.println(f);
                ASTVisitor visitor=new ChangeMethodVisitor(content,f);
                CompilationUnit unit=(CompilationUnit)parser.createAST(null);
                unit.accept(visitor);
                result.put(f,((ChangeMethodVisitor) visitor).changedMethods);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     *  返回一个Commit修改的所有文件中的域
     */
    public static HashMap<String,List<ChangedField>>getFields(Repository repository,String commitId){
        HashMap<String,List<ChangedField>> result = new HashMap<>();
        try{
            ObjectId curId = repository.resolve(commitId);
            Set<String>fs = getAllFileNamesModifiedByCommit(repository,commitId);
            for(String f:fs){
                String content = getFileFromCommit(repository,curId,f);
                ASTParser parser = ASTParser.newParser(AST.JLS10);
                parser.setSource(content.toCharArray());
                parser.setKind(ASTParser.K_COMPILATION_UNIT);
                //System.out.println(f);
                ASTVisitor visitor=new ChangedFieldVisitor(content,f);
                CompilationUnit unit=(CompilationUnit)parser.createAST(null);
                unit.accept(visitor);
                result.put(f,((ChangedFieldVisitor) visitor).changeFields);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 返回一个Commit修改的所有方法
     */
    public static List<ChangedMethod> getChangedMethods(HashMap<String,List<MyEdit>> edits,HashMap<String,List<ChangedMethod>> methods){
        List<ChangedMethod>result = new ArrayList<>();
        for(String f:edits.keySet()){
            List<MyEdit>myEdits = edits.get(f);
            List<ChangedMethod>myMethod = methods.get(f);
            if(myMethod == null)continue;
            for(ChangedMethod m:myMethod){
                boolean hasChanged = false;
                for(MyEdit edit:myEdits){
                    if(edit.start>=m.end)continue;
                    if(edit.end<m.start)continue;
                    hasChanged = true;
                    break;
                }
                if(hasChanged){
                    result.add(m);
                }
            }
        }
        return result;
    }

    /**
     *  返回一个Commit修改的所有域
     */
    public static List<ChangedField>getChangedFields(HashMap<String,List<MyEdit>> edits,HashMap<String,List<ChangedField>> fields) {
        List<ChangedField>result = new ArrayList<>();
        for(String f:edits.keySet()){
            List<MyEdit>myEdits = edits.get(f);
            List<ChangedField>myField = fields.get(f);
            if(myField==null)continue;
            for(ChangedField field:myField){
                boolean hasChanged = false;
                for(MyEdit edit:myEdits){
                    if(edit.start>=field.end)continue;
                    if(edit.end<field.start)continue;
                    hasChanged = true;
                    break;
                }
                if(hasChanged){
                    result.add(field);
                }
            }
        }
        return result;
    }

    /**
     * 必要的时候过滤一些复杂的Commit
     */
    public static List<RevCommit>getFilterCommit(Repository repository,List<RevCommit>commits){
        List<RevCommit>result = new ArrayList<>();
        for(RevCommit commit:commits){
            HashMap<String,List<MyEdit>> edits = getEdits(repository,commit.getId().getName());
            if(edits.size()>5)continue;
            result.add(commit);
        }
        return result;
    }

    /**
     * 返回所有修改中包含的定义与使用关系
     */
    public static List<DefineUseInfo>getDefineUseInfo(List<ChangedMethod>changedMethods,List<ChangedField>changedFields){
        List<DefineUseInfo> result = new ArrayList<>();
        for(ChangedMethod cm:changedMethods){
            for(ChangedField cf:changedFields){
                if(cm.accessField.contains(cf.name)){
                    int num = 0;
                    for(int i=0;i<cm.accessField.size();++i){
                        if(cm.accessField.get(i).equals(cf.name)){
                            num++;
                        }
                    }
                    result.add(new DefineUseInfo(cm.fullname,cf.fullname,num));
                }
            }
        }
        return result;
    }
}