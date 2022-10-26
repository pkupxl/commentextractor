package Git;

public class DefineUseInfo {
    public String funcName;
    public String fieldName;
    public int num;

    public DefineUseInfo(String funcName,String fieldName,int num){
        this.funcName = funcName;
        this.fieldName = fieldName;
        this.num = num;
    }

    public DefineUseInfo(){}
}
