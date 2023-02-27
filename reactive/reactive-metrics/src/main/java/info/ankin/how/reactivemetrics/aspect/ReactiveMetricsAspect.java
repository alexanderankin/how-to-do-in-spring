package info.ankin.how.reactivemetrics.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @see io.micrometer.core.annotation.Timed
 * @see io.micrometer.core.aop.TimedAspect
 * @see <a href=https://gist.github.com/alexanderankin/4954abd17488926823ebd1937f50a8b2>
 * https://gist.github.com/alexanderankin/4954abd17488926823ebd1937f50a8b2
 * </a>
 */
@Aspect
@Component
public class ReactiveMetricsAspect {
}
