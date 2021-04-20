package cn.krossframework.state.test.time;

import cn.krossframework.state.DefaultLazyTime;
import cn.krossframework.state.Time;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TimeTest {

	@Test public void test() throws InterruptedException {
		Optional<List<Object>> objects = Optional.of(Collections.emptyList());
		Optional<List<Object>> list = objects.filter(l -> !l.isEmpty());
	}
}
