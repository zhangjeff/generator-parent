import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

/**
 * 生成mapper工具类<br>
 * 也可以用maven形式生成，命令mybatis-generator:generate
 * @author wwy
 * @date 2015年8月30日 下午8:41:37
 *
 */
public class Generator {

    public static String generatorConfig = "generatorConfig.xml";

    public static void main(String[] args) {
        // 清理src/main/java下的文件
        File fileDir = new File("generator-mbg/src/main/java");
        try {
            FileUtils.cleanDirectory(fileDir);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        List<String> warnings = new ArrayList<>();
        boolean overwrite = true;
        File configFile = new File(Generator.class.getClassLoader().getResource(Generator.generatorConfig).getPath());
        try {
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configFile);
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);// 生成baseEntity、javaMapper、mapperXml
            ServiceGenerator.main(args);// 生成entity、service、serviceImpl
            System.out.println("生成完毕!");
        } catch (SQLException | IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (XMLParserException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
