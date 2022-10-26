package Git;

public class MyEdit {
    public String path;
    public int start;
    public int end;
    public MyEdit(String path, int start, int end){
        this.path = path;
        this.start = start;
        this.end = end;
    }
}
