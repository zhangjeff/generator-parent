
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.codehaus.plexus.util.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.NullProgressCallback;

import freemarker.template.Template;
import generator.constant.Constants;

/**
 * 
 * @author wwy
 * @date 2015年9月6日下午4:57:03
 */
public class ServiceGenerator {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        List<String> warnings = new ArrayList<>();
        File configFile = new File(Generator.class.getClassLoader().getResource(Generator.generatorConfig).getPath());
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config;
        try {
            config = cp.parseConfiguration(configFile);
            List<Context> contexts = config.getContexts();

            initXml(warnings, contexts);// 初始化xml，获取introspectedTables信息

            // 加载freemarker配置和模板
            freemarker.template.Configuration cfg = new freemarker.template.Configuration(
                    freemarker.template.Configuration.VERSION_2_3_22);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setDirectoryForTemplateLoading(new File("generator-mbg/src/main/resources/template/"));

            for (Context context : contexts) {
                List<TableConfiguration> configuration = context.getTableConfigurations();
                JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = context
                        .getJavaModelGeneratorConfiguration();
                String targetProject = javaModelGeneratorConfiguration.getTargetProject();
                String modelTarget = javaModelGeneratorConfiguration.getTargetPackage();
                String modelPath = modelTarget.replace(".", "/");
                String servicePath = modelTarget.substring(0, modelTarget.lastIndexOf(".")) + ".service";
                String controllerPath = modelTarget.substring(0, modelTarget.lastIndexOf(".")) + ".controller";
                String serviceImplPath = servicePath + ".impl";
                String mapperPath = context.getJavaClientGeneratorConfiguration().getTargetPackage();
                String domain = null;
                context.getTargetRuntime();
                Field filed = context.getClass().getDeclaredField("introspectedTables");
                filed.setAccessible(true);
                List<IntrospectedTable> introspectedTables = (List<IntrospectedTable>) filed.get(context);
                if (introspectedTables == null || introspectedTables.size() == 0) {
                    System.err.println("没有可生成的表!请检查数据库连接及生成表配置");
                    return;
                }
                for (TableConfiguration tableConfiguration : configuration) {
                    domain = tableConfiguration.getDomainObjectName();// 实体类名称
                    String domainPackage = tableConfiguration.getProperty("domainPackage");// xml中配置的domainPackage属性
                    String smallDomainName = StringUtils.lowercaseFirstLetter(domain);
                    String primaryKeyType = null;// 主键类型
                    for (IntrospectedTable introspectedTable : introspectedTables) {
                        String domainObject = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
                        if (domainObject.equals(domain)) {
                            primaryKeyType = introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType()
                                    .getShortName();// 主键类型
                        }
                    }
                    if (primaryKeyType == null) {
                        System.out.println(domain + "表在数据库中不存在或没有主键!停止" + domain + "表生成");
                        break;
                    }

                    // 开始生成entity和example
                    Map<String, Object> entityMap = new HashMap<>();
                    entityMap.put("package", modelTarget);
                    entityMap.put("domainName", domain);
                    entityMap.put("baseDomain", modelTarget + "." + domainPackage + ".Base" + domain);
                    entityMap.put("paginationPackage", Constants.PAGINATION_PACKAGE);
                    generateEntity(cfg, entityMap, targetProject + "/" + modelPath + "/" + domain + ".java");// 生成entity
                    generateEntityExample(cfg, entityMap,
                            targetProject + "/" + modelPath + "/" + domain + "Example.java");// 生成entityExample

                    // 开始生成javaMapper
                    Map<String, Object> javaMapperMap = new HashMap<>();
                    javaMapperMap.put("package", mapperPath);
                    javaMapperMap.put("domainName", domain);
                    javaMapperMap.put("primaryKeyType", primaryKeyType);
                    javaMapperMap.put("baseModelPackage", modelTarget);
                    javaMapperMap.put("genericMapperPackage", Constants.GENERIC_MAPPER_PACKAGE);
                    generateJavaMap(cfg, javaMapperMap,
                            targetProject + "/" + mapperPath.replace(".", "/") + "/" + domain + "Mapper.java");// 生成entity

                    // 开始生成service,serviceImpl
                    Map<String, Object> serviceMap = new HashMap<>();
                    serviceMap.put("domainName", domain);
                    serviceMap.put("package", servicePath);
                    serviceMap.put("primaryKeyType", primaryKeyType);
                    serviceMap.put("baseModelPackage", modelTarget);
                    serviceMap.put("genericServicePackage", Constants.GENERIC_SERVICE_PACKAGE);
                    serviceMap.put("genericMapperPackage", Constants.GENERIC_MAPPER_PACKAGE);
                    serviceMap.put("genericServiceImplPackage", Constants.GENERIC_SERVICEIMPL_PACKAGE);
                    serviceMap.put("smallDomainName", smallDomainName);
                    serviceMap.put("mapperPackage", mapperPath + "." + domain + "Mapper");
                    generateService(cfg, serviceMap,
                            targetProject + "/" + servicePath.replace(".", "/") + "/" + domain + "Service.java");// 生成service
                    generateServiceImpl(cfg, serviceMap, targetProject + "/" + serviceImplPath.replace(".", "/") + "/"
                            + domain + "ServiceImpl.java");// 生成serviceImpl


                    // 开始生成Controller
                    Map<String, Object> controllerMap = new HashMap<>();
                    controllerMap.put("domainName", domain);
                    controllerMap.put("classAuthor", "jeff zhang");

                    //我要获取当前的日期
                    Date date = new Date();
                    //设置要获取到什么样的时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    //获取String类型的时间
                    String controllerPackage = "controller";
                    String createdate = sdf.format(date);
                    controllerMap.put("currDate", createdate);
                    controllerMap.put("packageName", controllerPath);
                    controllerMap.put("package", controllerPath);
                    controllerMap.put("moduleName", "");
                    controllerMap.put("primaryKeyType", primaryKeyType);
                    controllerMap.put("baseModelPackage", controllerPath);
                    controllerMap.put("genericServicePackage", Constants.GENERIC_SERVICE_PACKAGE);
                    controllerMap.put("genericMapperPackage", Constants.GENERIC_MAPPER_PACKAGE);
                    controllerMap.put("genericServiceImplPackage", Constants.GENERIC_SERVICEIMPL_PACKAGE);
                    controllerMap.put("smallDomainName", smallDomainName);
//                    controllerMap.put("mapperPackage", mapperPath + "." + domain + "Mapper");
                    generateController(cfg, controllerMap,
                            targetProject + "/" + controllerPath.replace(".", "/") + "/" + domain + "Controller.java");// 生成entity
                }
            }
        } catch (IOException | XMLParserException | NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化xml
     * @param warnings
     * @param contexts
     */
    private static void initXml(List<String> warnings, List<Context> contexts) {
        ProgressCallback callback = new NullProgressCallback();
        int totalSteps = 0;
        for (Context context : contexts) {
            totalSteps += context.getIntrospectionSteps();
        }
        callback.introspectionStarted(totalSteps);
        for (Context context : contexts) {
            try {
                context.introspectTables(callback, warnings, null);
            } catch (SQLException | InterruptedException | SecurityException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public static void generate(String templateName, freemarker.template.Configuration cfg, Map<String, Object> map,
            String filePath) {
        Template template;
        try {
            template = cfg.getTemplate(templateName);
            String content = renderTemplate(template, map);
            writeFile(new File(filePath), content, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成 Entity
     * @param cfg
     * @throws IOException
     */
    public static void generateEntity(freemarker.template.Configuration cfg, Map<String, Object> map, String filePath)
            throws IOException {
        generate("model.ftl", cfg, map, filePath);
    }

    /**
     * 生成 EntityExample
     * @param cfg
     * @throws IOException
     */
    public static void generateEntityExample(freemarker.template.Configuration cfg, Map<String, Object> map,
            String filePath) throws IOException {
        generate("modelExample.ftl", cfg, map, filePath);
    }

    public static void generateJavaMap(freemarker.template.Configuration cfg, Map<String, Object> map, String filePath)
            throws IOException {
        generate("mapper.ftl", cfg, map, filePath);
    }

    public static void generateService(freemarker.template.Configuration cfg, Map<String, Object> map, String filePath)
            throws IOException {
        generate("service.ftl", cfg, map, filePath);
    }

    public static void generateServiceImpl(freemarker.template.Configuration cfg, Map<String, Object> map,
            String filePath) throws IOException {
        generate("serviceImpl.ftl", cfg, map, filePath);
    }

    public static void generateController(freemarker.template.Configuration cfg, Map<String, Object> map,
                                           String filePath) throws IOException {
        generate("controller.ftl", cfg, map, filePath);
    }

    private static String renderTemplate(Template template, Object model) {
        try {
            StringWriter result = new StringWriter();
            template.process(model, result);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Writes, or overwrites, the contents of the specified file
     * 
     * @param file
     * @param content
     */
    private static void writeFile(File file, String content, String fileEncoding) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        int index = file.getPath().lastIndexOf(File.separator);
        File dir = new File(file.getPath().substring(0, index));
        dir.mkdirs();
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file, false);
        OutputStreamWriter osw;
        if (fileEncoding == null) {
            osw = new OutputStreamWriter(fos);
        } else {
            osw = new OutputStreamWriter(fos, fileEncoding);
        }

        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(content);
        bw.close();
    }
}
