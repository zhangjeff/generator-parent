package generator.plugins;

import java.util.List;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * 序列化插件，默认实现java.io.Serializable，但并不生成默认序列化字符串，需手动生成，尽量不依赖jdk生成
 * 
 * @author wwy
 * @date 2015年9月5日下午9:30:09
 */
public class EntitySerializablePlugin extends PluginAdapter {

    private FullyQualifiedJavaType serializable;
    private boolean suppressJavaInterface;

    public EntitySerializablePlugin() {
        super();
        serializable = new FullyQualifiedJavaType("java.io.Serializable"); //$NON-NLS-1$
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        suppressJavaInterface = Boolean.valueOf(properties.getProperty("suppressJavaInterface")); //$NON-NLS-1$
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // System.out.println(topLevelClass.getType());
        // System.out.println(introspectedTable.getFullyQualifiedTable().getFullyQualifiedTableNameAtRuntime());
        // ServiceGenerator.strs.add("a");
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        makeSerializable(topLevelClass, introspectedTable);
        // GeneratedCriteria 实现序列化
        InnerClass criteria = null, criterion = null;
        for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
            if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
                criteria = innerClass;
            }
            if ("Criterion".equals(innerClass.getType().getShortName())) {
                criterion = innerClass;
            }
            if (criteria != null && criterion != null) {
                break;
            }
        }
        criteria.addSuperInterface(serializable);
        criterion.addSuperInterface(serializable);
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    protected void makeSerializable(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!suppressJavaInterface) {
            topLevelClass.addImportedType(serializable);
            topLevelClass.addSuperInterface(serializable);
        }
    }
}
