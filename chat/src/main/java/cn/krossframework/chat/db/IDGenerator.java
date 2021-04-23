package cn.krossframework.chat.db;

public interface IDGenerator {

    String suffix();

    String generate();
}
