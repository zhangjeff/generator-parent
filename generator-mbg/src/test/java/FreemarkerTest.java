import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * 
 * @author wwy
 * @date 2015年9月6日下午10:06:46
 */
public class FreemarkerTest {

    public static void main(String[] args) throws IOException, TemplateException {
        long time1 = System.currentTimeMillis();
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("myTemplate", "这是一个测试模板，测试变量输出。\r\n" + "姓名：${name}\r\n" + "年龄：${age}");
        cfg.setTemplateLoader(stringLoader);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        Map<String, Object> root = new HashMap<>();
        root.put("name", "Big Joe");
        root.put("age", "22");
        Template temp = cfg.getTemplate("myTemplate");
        StringWriter writer = new StringWriter();
        temp.process(root, writer);
        System.out.println("内容-----" + writer.toString());
        writer.close();
        long time2 = System.currentTimeMillis();
        System.out.println((time2 - time1));
    }
}
