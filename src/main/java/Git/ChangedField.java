package Git;

/**
 * 代表一个修改的域
 */
public class ChangedField {
    public String name;
    public String fullname;
    public int start;
    public int end;
    public String fileName;
    public ChangedField(String name,String fullname,int start,int end,String fileName){
        this.name = name;
        this.fullname = fullname;
        this.start = start;
        this.end = end;
        this.fileName = fileName;
    }
}
