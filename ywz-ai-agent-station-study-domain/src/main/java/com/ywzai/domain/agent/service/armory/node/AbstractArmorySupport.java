package com.ywzai.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import jakarta.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @Author: ywz @CreateTime: 2025-09-19 @Description: 策略树配置抽象类 @Version: 1.0
 */
@Slf4j
public abstract class AbstractArmorySupport
    extends AbstractMultiThreadStrategyRouter<
        ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> {

  @Resource private ApplicationContext applicationContext;

  @Override
  protected void multiThread(
      ArmoryCommandEntity armoryCommandEntity,
      DefaultArmoryStrategyFactory.DynamicContext dynamicContext)
      throws ExecutionException, InterruptedException, TimeoutException {}

  public synchronized <T> void registerBean(String beanName, Class<T> beanClass, T beanInstance) {
    DefaultListableBeanFactory beanFactory =
        (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
    BeanDefinitionBuilder beanDefinitionBuilder =
        BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> beanInstance);
    BeanDefinition rawBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
    rawBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
    if (beanFactory.containsBeanDefinition(beanName)) {
      beanFactory.removeBeanDefinition(beanName);
    }
    beanFactory.registerBeanDefinition(beanName, rawBeanDefinition);
    log.info("成功注册Bean: {}", beanName);
  }

  protected <T> T getBean(String beanName) {
    return (T) applicationContext.getBean(beanName);
  }
}
