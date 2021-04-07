package cn.krossframework.web;

import cn.krossframework.state.Task;
import cn.krossframework.state.Worker;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.web.cat.CatTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    private final WorkerManager workerManager;

    public WebApplication(WorkerManager workerManager) {
        this.workerManager = workerManager;
    }

    @RequestMapping("/addCat/{id}")
    public void addCat(@PathVariable("id") Long id) {
        this.workerManager.enter(new Worker.GroupIdTaskPair(id, new Task() {
        }));
    }

    @RequestMapping("/killCat/{id}")
    public void killCat(@PathVariable("id") Long id) {
        this.workerManager.addTask(new Worker.GroupIdTaskPair(id, new CatTask(1)));
    }
}
