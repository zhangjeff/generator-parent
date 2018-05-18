package generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * MySQL 返回主键属性插件<br>
 * 在insert节点中增加useGeneratedKeys和keyProperty属性，增加记录后可以直接返回生成的自增长主键
 *
 * @author wwy
 * @version 1.0.0.0
 */
public final class MySQLGeneratorPrimaryKeyPlugin extends PluginAdapter {

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (introspectedTable.getPrimaryKeyColumns().size() == 1) {
            FullyQualifiedJavaType fullyQualifiedJavaType = introspectedTable.getPrimaryKeyColumns().get(0)
                    .getFullyQualifiedJavaType();
            String primaryKeyClassTypeName = fullyQualifiedJavaType.getFullyQualifiedName();
            if ("java.lang.Integer".equals(primaryKeyClassTypeName)) {
                element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                element.addAttribute(new Attribute("keyProperty",
                        introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()));
            }
        } else {
            System.out.println("主键不止一个，不生成useGeneratedKeys和keyProperty属性!");
        }
        return super.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (introspectedTable.getPrimaryKeyColumns().size() == 1) {
            FullyQualifiedJavaType fullyQualifiedJavaType = introspectedTable.getPrimaryKeyColumns().get(0)
                    .getFullyQualifiedJavaType();
            String primaryKeyClassTypeName = fullyQualifiedJavaType.getFullyQualifiedName();
            if ("java.lang.Integer".equals(primaryKeyClassTypeName)) {
                element.addAttribute(new Attribute("useGeneratedKeys", "true"));
                element.addAttribute(new Attribute("keyProperty",
                        introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()));
            }
        } else {
            System.out.println("主键不止一个，不生成useGeneratedKeys和keyProperty属性!");
        }
        return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
}