package Git;

import java.util.List;

/**
 * 代表一个方法
 */
public class ChangedMethod {
    public String name;
    public String fullname;
    public int start;
    public int end;
    public String fileName;
    public List<String>accessField;
    public String content;
    public String comment;
    public ChangedMethod(String name, String fullname, int start, int end,String fileName,List<String>accessField,String content, String comment){
        this.name = name;
        this.fullname = fullname;
        this.start = start;
        this.end = end;
        this.fileName = fileName;
        this.accessField = accessField;
        this.content = content;
        this.comment = comment;
    }
}