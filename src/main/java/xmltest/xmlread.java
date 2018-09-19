package xmltest;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import org.dom4j.*;

public class xmlread
{
    public static void main(String[] args) throws IOException, InterruptedException, DocumentException {
        Properties properties = new Properties();
        InputStream in = xmlread.class.getClassLoader().getResourceAsStream("config.properties");
        properties.load(in);
        String scada_cim_load = properties.getProperty("scada_cim_load");
        String scada_cim_out = properties.getProperty("scada_cim_out");
        File filecatalog = new File(scada_cim_load);
        Map<String, Integer> filechangetime = new HashMap<String, Integer>();
        while (true) {
            File[] FileCim = filecatalog.listFiles();
            System.out.println("需要解析的CIM模型文件数量：" + FileCim.length);
            long starttime = System.currentTimeMillis();
            for (int i = 0; i < FileCim.length; ++i) {
                int catalogstart = FileCim[i].toString().lastIndexOf(File.separator);
                int splitindex1 = FileCim[i].toString().indexOf("__");
                int splitindex2 = FileCim[i].toString().lastIndexOf("__");
                String cityname = FileCim[i].toString().substring(catalogstart + 1, splitindex1);
                String changetime = FileCim[i].toString().substring(splitindex1 + 2, splitindex2);
                if (filechangetime.get(cityname) == null || filechangetime.get(cityname) < Integer.parseInt(changetime)) {
                    filechangetime.put(cityname, Integer.valueOf(changetime));
                    String fileoutname = FileCim[i].toString().substring(catalogstart + 1, FileCim[i].toString().length() - 4);
                    File fileout = new File(scada_cim_out, fileoutname + ".txt");
                    if (!fileout.getParentFile().exists()) {
                        fileout.getParentFile().mkdirs();
                        System.out.println("create!");
                    }
                    fileout.createNewFile();
                        Xmlclassify xmlclassify = new Xmlclassify(FileCim[i]);
                        EMSlinefullname EMSCIM = new EMSlinefullname(fileout, xmlclassify);
                        EMSCIM.energyconsumerThread();

                }
            }
            long endtime = System.currentTimeMillis();
            System.out.println("所有cim模型解析完毕，用时：" + (endtime - starttime) / 1000+"s");
           TimeUnit.DAYS.sleep(1L);
        }
    }
}
