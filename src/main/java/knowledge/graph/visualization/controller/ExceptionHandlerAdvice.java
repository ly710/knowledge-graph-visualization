package knowledge.graph.visualization.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
@Slf4j
public class ExceptionHandlerAdvice
{
    @ExceptionHandler(Exception.class)
    public Result<String> ExceptionHandler(Exception e)
    {
        e.printStackTrace();
        return new Result<String>(1, "error", null);
    }
}
