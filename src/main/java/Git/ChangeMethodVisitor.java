package Git;

import org.eclipse.jdt.core.dom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChangeMethodVisitor extends ASTVisitor {
    public List<ChangedMethod>changedMethods = new ArrayList<>();
    public String fileContent;
    public String filename;
    public ChangeMethodVisitor(String content,String filename){
        //System.out.println("代码长度"+fileContent.length());
        this.fileContent = content;
        this.filename = filename;
    }

    public boolean visit(TypeDeclaration node){
        MethodDeclaration[] methodDeclarations = node.getMethods();
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            ChangedMethod myMethod = createMethod(methodDeclaration, NameResolver.getFullName(node));
            if(myMethod!=null){
                changedMethods.add(myMethod);
            }
        }
        return false;
    }

    public ChangedMethod createMethod(MethodDeclaration node,String belongTo){
        String name = node.getName().getFullyQualifiedName();
        String params = String.join(", ", (List<String>) node.parameters().stream().map(n -> {
            SingleVariableDeclaration param = (SingleVariableDeclaration) n;
            return (Modifier.isFinal(param.getModifiers()) ? "final " : "") + param.getType().toString() + " " + param.getName().getFullyQualifiedName();
        }).collect(Collectors.toList()));
        String fullName = belongTo + "." + name + "( " + params + " )";
        int startline;
        int endline;
        String comment = "";
        String content = "";
        try{
            if(node.getBody() == null)return null;
            startline = fileContent.substring(0,node.getBody().getStartPosition()).split("\\n").length;
            endline = fileContent.substring(0,node.getBody().getStartPosition()+node.getBody().getLength()).split("\\n").length;
            if(node.getJavadoc()!=null){
                comment = node.getJavadoc().toString();
            }
            content = node.getBody().toString();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        List<String>accessFields = getAccessField(node);
        ChangedMethod info = new ChangedMethod(name, fullName ,startline,endline,filename,accessFields,content,comment);
        return info;
    }

    public List<String>getAccessField(MethodDeclaration node){
        List<String>result = new ArrayList<>();
        Block methodBody = node.getBody();
        parseMethodBody(methodBody,result);
        return result;
    }

    private void parseMethodBody(Block methodBody,List<String>result) {
        if (methodBody == null)
            return;
        List<Statement> statementList = methodBody.statements();
        List<Statement> statements = new ArrayList<>();
        for (int i = 0; i < statementList.size(); i++) {
            statements.add(statementList.get(i));
        }
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i).getNodeType() == ASTNode.BLOCK) {
                List<Statement> blockStatements = ((Block) statements.get(i)).statements();
                for (int j = 0; j < blockStatements.size(); j++) {
                    statements.add(i + j + 1, blockStatements.get(j));
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.ASSERT_STATEMENT) {
                Expression expression = ((AssertStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(expression,result);
                }
                expression = ((AssertStatement) statements.get(i)).getMessage();
                if (expression != null) {
                    parseExpression(expression,result);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.DO_STATEMENT) {
                Expression expression = ((DoStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(expression,result);
                }
                Statement doBody = ((DoStatement) statements.get(i)).getBody();
                if (doBody != null) {
                    statements.add(i + 1, doBody);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT) {
                Expression expression = ((EnhancedForStatement) statements.get(i)).getExpression();
                Type type = ((EnhancedForStatement) statements.get(i)).getParameter().getType();
                if (expression != null) {
                    parseExpression(expression,result);
                }
                Statement forBody = ((EnhancedForStatement) statements.get(i)).getBody();
                if (forBody != null) {
                    statements.add(i + 1, forBody);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
                Expression expression = ((ExpressionStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression( expression,result);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.FOR_STATEMENT) {
                List<Expression> list = ((ForStatement) statements.get(i)).initializers();
                for (int j = 0; j < list.size(); j++) {
                    parseExpression(list.get(j),result);
                }
                Expression expression = ((ForStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(expression,result);
                }
                Statement forBody = ((ForStatement) statements.get(i)).getBody();
                if (forBody != null) {
                    statements.add(i + 1, forBody);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.IF_STATEMENT) {
                Expression expression = ((IfStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(expression,result);
                }
                Statement thenStatement = ((IfStatement) statements.get(i)).getThenStatement();
                Statement elseStatement = ((IfStatement) statements.get(i)).getElseStatement();
                if (elseStatement != null) {
                    statements.add(i + 1, elseStatement);
                }
                if (thenStatement != null) {
                    statements.add(i + 1, thenStatement);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.RETURN_STATEMENT) {
                Expression expression = ((ReturnStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(expression,result);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.SWITCH_STATEMENT) {
                Expression expression = ((SwitchStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(expression,result);
                }
                List<Statement> switchStatements = ((SwitchStatement) statements.get(i)).statements();
                for (int j = 0; j < switchStatements.size(); j++) {
                    statements.add(i + j + 1, switchStatements.get(j));
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.THROW_STATEMENT) {
                Expression expression = ((ThrowStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression(expression,result);
                }
            }
            else if (statements.get(i).getNodeType() == ASTNode.TRY_STATEMENT) {
                Statement tryStatement = ((TryStatement) statements.get(i)).getBody();
                if (tryStatement != null) {
                    statements.add(i + 1, tryStatement);
                }
                continue;
            }
            else if (statements.get(i).getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
                Type type = ((VariableDeclarationStatement) statements.get(i)).getType();
                ((VariableDeclarationStatement) statements.get(i)).fragments().forEach(n -> parseExpression(((VariableDeclaration) n).getInitializer(),result));
            }
            else if (statements.get(i).getNodeType() == ASTNode.WHILE_STATEMENT) {
                Expression expression = ((WhileStatement) statements.get(i)).getExpression();
                if (expression != null) {
                    parseExpression( expression,result);
                }
                Statement whileBody = ((WhileStatement) statements.get(i)).getBody();
                if (whileBody != null) {
                    statements.add(i + 1, whileBody);
                }
            }
        }
    }

    private void parseExpression(Expression expression,List<String>result) {
        if (expression == null) {
            return;
        }
        if(expression.getNodeType()==ASTNode.FIELD_ACCESS){
            result.add(((FieldAccess)expression).getName().toString());
        } else if (expression.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
            List<Expression> expressions = ((ArrayInitializer) expression).expressions();
            for (Expression expression2 : expressions) {
                parseExpression(expression2,result);
            }
        }
        else if (expression.getNodeType() == ASTNode.CAST_EXPRESSION) {
            parseExpression(((CastExpression) expression).getExpression(),result);
        }
        else if (expression.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
            parseExpression(((ConditionalExpression) expression).getExpression(),result);
            parseExpression(((ConditionalExpression) expression).getElseExpression(),result);
            parseExpression(((ConditionalExpression) expression).getThenExpression(),result);
        }
        else if (expression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
            parseExpression(((InfixExpression) expression).getLeftOperand(),result);
            parseExpression(((InfixExpression) expression).getRightOperand(),result);
        }
        else if (expression.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
            parseExpression(((InstanceofExpression) expression).getLeftOperand(),result);
        }
        else if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
            parseExpression(((ParenthesizedExpression) expression).getExpression(),result);
        }
        else if (expression.getNodeType() == ASTNode.POSTFIX_EXPRESSION) {
            parseExpression(((PostfixExpression) expression).getOperand(),result);
        }
        else if (expression.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
            parseExpression(((PrefixExpression) expression).getOperand(),result);
        }
        else if (expression.getNodeType() == ASTNode.THIS_EXPRESSION) {
            parseExpression(((ThisExpression) expression).getQualifier(),result);
        }
        else if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
            List<Expression> arguments = ((MethodInvocation) expression).arguments();
            for (Expression exp : arguments)
                parseExpression(exp,result);
            parseExpression(((MethodInvocation) expression).getExpression(),result);
        }
        else if (expression.getNodeType() == ASTNode.ASSIGNMENT) {
            parseExpression(((Assignment) expression).getLeftHandSide(),result);
            parseExpression(((Assignment) expression).getRightHandSide(),result);
        }
        else if (expression.getNodeType() == ASTNode.QUALIFIED_NAME) {
            parseExpression(((QualifiedName) expression).getQualifier(),result);
        }
    }
}
