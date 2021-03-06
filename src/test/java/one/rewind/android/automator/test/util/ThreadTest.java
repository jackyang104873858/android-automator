package one.rewind.android.automator.test.util;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author maxuefeng[m17793873123@163.com]
 */
public class ThreadTest {


	private static Object call() {
		do {
			System.out.println("阻塞线程");
		} while (true);
	}

	@Test
	public void testExecutor() {
		ExecutorService service = Executors.newCachedThreadPool();

		service.submit(ThreadTest::call);

		System.out.println("Main Thread is Here");
	}

	@Test
	public void testExecutor1() {
		Thread thread = new Thread(() -> {
			try {
				System.out.println("hello world");
				throw new NullPointerException("null point exception");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}

}
