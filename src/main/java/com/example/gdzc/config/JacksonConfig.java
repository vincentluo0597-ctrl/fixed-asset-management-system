package com.example.gdzc.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module hibernateModule() {
        Hibernate5JakartaModule module = new Hibernate5JakartaModule();
        // 不强制懒加载，避免在没有Session的情况下加载代理
        module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
        // 使用普通集合替换持久化集合，避免序列化代理集合
        module.configure(Hibernate5JakartaModule.Feature.REPLACE_PERSISTENT_COLLECTIONS, true);
        return module;
    }
}