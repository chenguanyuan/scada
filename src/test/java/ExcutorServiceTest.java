import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chengy on 2018/9/14.
 */
public class ExcutorServiceTest {
    public static void main(String[] args) {
        System.out.println("Runtime.getRuntime().availableProcessors():"+Runtime.getRuntime().availableProcessors());
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ThreadPoolExecutor tpe = (ThreadPoolExecutor)fixedThreadPool;
        Thread th1=new Thread(new myThread());
        Thread th2=new Thread(new myThread());
        Thread th3=new Thread(new myThread());
        Thread th4=new Thread(new myThread());
        Thread th5=new Thread(new myThread());
        Thread th6=new Thread(new myThread());

        tpe.execute(th1);
        tpe.execute(th2);
        tpe.execute(th3);
        tpe.execute(th4);
        tpe.execute(th5);
        tpe.execute(th6);


        int activeCount;
        do {
            activeCount = tpe.getActiveCount();
        } while (activeCount != 0);
        fixedThreadPool.shutdown();

    }

}

class myThread implements Runnable{

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+"正在运行......");
    }
}
