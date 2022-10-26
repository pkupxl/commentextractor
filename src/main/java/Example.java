import Git.ChangedMethod;
import Git.GitUtil;
import Git.MyEdit;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Jira.JiraUtil.get_comments_body;
import static Jira.JiraUtil.login_jira;

public class Example {
    /**
     * 爬取一个Issue的内容
     */
    public static void crawIssue(String issueNum){
        //https://issues.apache.org/jira的账号,可以注册一个
        String username = "CodeComment";
        String password = "CodeComment";
        try {
            final JiraRestClient restClient = login_jira(username, password);
            final NullProgressMonitor pm = new NullProgressMonitor();
            Issue issue = restClient.getIssueClient().getIssue(issueNum, pm);
            HashMap<String, List<String>> info = new HashMap<>();
            List<String> summary = new ArrayList<>();
            summary.add(issue.getSummary());
            List<String> description = new ArrayList<>();
            description.add(issue.getDescription());
            List<String> comments = new ArrayList<>();
            for(String s:get_comments_body(issue)){
                comments.add(s);
            }
            System.out.println(summary);
            System.out.println(description);
            System.out.println(comments);
            info.put("summary",summary);
            info.put("description",description);
            info.put("comments",comments);

            //可以先离线保存下来
            ReadWriteUtil.saveJson("D:\\CodeCommentMiner\\commentextractor\\LUCENE_ISSUE\\"+issueNum+".json",info);
            System.out.println(issueNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIssueId(String commitMessage){
        String pattern = "LUCENE-[0-9]+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(commitMessage);
        if (m.find( )) {
            return m.group(0);
        }
        return null;
    }


    public static void commitAnalysisExample(){
        try{
            //待分析Git仓库的路径
            String repo = "D:\\Code\\lucene\\.git";
            Repository repository = new FileRepository(repo);
            List<RevCommit> commits = GitUtil.getCommits(repository);
            for(RevCommit commit :commits){
                String message = commit.getFullMessage();
                Set<String> fs = GitUtil.getAllFileNamesModifiedByCommit(repository,commit.getName());
                System.out.println(commit.getName());
                System.out.println(message);
                System.out.println(getIssueId(message));
                System.out.println(fs);
                HashMap<String,List<MyEdit>>edits = GitUtil.getEdits(repository,commit.getName());
                HashMap<String,List<ChangedMethod>> methods = GitUtil.getMethods(repository,commit.getName());
                List<ChangedMethod> changedMethods = GitUtil.getChangedMethods(edits,methods);

                for(ChangedMethod m:changedMethods){
                    System.out.println(m.fullname);
                    System.out.println(m.comment);
                    System.out.println(m.content);
                }
                break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws URISyntaxException {
        //crawIssue("LUCENE-10676");

        commitAnalysisExample();
    }
}
