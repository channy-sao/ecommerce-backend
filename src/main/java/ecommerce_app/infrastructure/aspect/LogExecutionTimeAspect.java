package ecommerce_app.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogExecutionTimeAspect {

  @Around("@annotation(LogExecutionTime)")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

    long start = System.currentTimeMillis();

    Object result = joinPoint.proceed(); // execute method

    long time = System.currentTimeMillis() - start;

    log.info("{} executed in {} ms", joinPoint.getSignature(), time);

    return result;
  }
}
