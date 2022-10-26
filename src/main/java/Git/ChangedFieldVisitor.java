package Git;

import org.eclipse.jdt.core.dom.*;
import java.util.ArrayList;
import java.util.List;

public class ChangedFieldVisitor extends ASTVisitor {
    public List<ChangedField> changeFields = new ArrayList<>();
    public String content;
    public String filename;
    public ChangedFieldVisitor(String content,String filename){
        this.content = content;
        this.filename = filename;
    }

    public boolean visit(TypeDeclaration node){
        FieldDeclaration[] fieldDeclarations = node.getFields();
        for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
            addField(fieldDeclaration, NameResolver.getFullName(node));
        }
        return false;
    }

    public void addField(FieldDeclaration node,String belongTo){
        node.fragments().forEach(n -> {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) n;
            String name = fragment.getName().getFullyQualifiedName();
            String fullName = belongTo + "." + name;
            int startline = content.substring(0,fragment.getStartPosition()).split("\\n").length;
            changeFields.add(new ChangedField(name, fullName, startline, startline, filename));
        });
    }
}
