package org.gourd.hu.base.handler;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.gourd.hu.base.exceptions.BusinessException;
import org.gourd.hu.base.request.bean.RequestDetail;
import org.gourd.hu.base.request.holder.RequestDetailThreadLocal;
import org.gourd.hu.base.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * 统一异常处理器
 * @author gourd
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

	/**
	 * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		log.debug("请求有参数才进来:{} ",binder.getObjectName());
	}

	/**
	 * 把值绑定到Model中，使全局@RequestMapping可以获取到该值
	 * @param model
	 */
	@ModelAttribute
	public void addAttributes(Model model) {
		model.addAttribute("author", "gourd.hu");
	}

	/**
	 * 处理自定义业务异常
	 * @param ex
	 * @return
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(value = BusinessException.class)
	public BaseResponse handleException(BusinessException ex) {
		// 打印堆栈信息
		printRequestDetail();
		printApiCodeException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
		return BaseResponse.fail(ex.getMessage());
	}

	/**
	 * 非法参数验证异常
	 *
	 * @param ex
	 * @return
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler({MethodArgumentNotValidException.class})
	public BaseResponse handleException(MethodArgumentNotValidException ex) {
		printRequestDetail();
		BindingResult bindingResult = ex.getBindingResult();
		List<String> list = new ArrayList<>();
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		for (FieldError fieldError : fieldErrors) {
			list.add(fieldError.getDefaultMessage());
		}
		Collections.sort(list);
		log.error(getApiCodeString(HttpStatus.BAD_REQUEST) + ":" + JSON.toJSONString(list));
		return BaseResponse.fail(HttpStatus.BAD_REQUEST,ex.getMessage(), list);
	}

	/**
	 * 非法参数验证异常
	 *
	 * @param ex
	 * @return
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler({BindException.class})
	public BaseResponse handleException(BindException ex) {
		printRequestDetail();
		BindingResult bindingResult = ex.getBindingResult();
		List<String> list = new ArrayList<>();
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		for (FieldError fieldError : fieldErrors) {
			list.add(fieldError.getDefaultMessage());
		}
		Collections.sort(list);
		log.error(getApiCodeString(HttpStatus.BAD_REQUEST) + ":" + JSON.toJSONString(list));
		return BaseResponse.fail(HttpStatus.BAD_REQUEST,ex.getMessage(), list);
	}

	/**
	 * 404异常处理
	 * @param ex
	 * @return
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(NoHandlerFoundException.class)
	public BaseResponse handleException(NoHandlerFoundException ex) {
		printRequestDetail();
		return BaseResponse.fail(HttpStatus.NOT_FOUND,ex.getMessage());
	}

	/**
	 * 不支持方法异常处理
	 *
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse handleException(HttpRequestMethodNotSupportedException ex) {
		printRequestDetail();
		printApiCodeException(HttpStatus.METHOD_NOT_ALLOWED, ex);
		return BaseResponse.fail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
	}

	/**
	 * 默认的异常处理
	 *
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(value = Exception.class)
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse handleException(Exception ex) {
		printRequestDetail();
		printApiCodeException(INTERNAL_SERVER_ERROR, ex);
		return BaseResponse.fail(ex.getMessage());
	}

	/**
	 * 获取httpStatus格式化字符串
	 *
	 * @param httpStatus
	 * @return
	 */
	private String getApiCodeString(HttpStatus httpStatus) {
		if (httpStatus != null) {
			return String.format("errorCode: %s, errorMessage: %s", httpStatus.value(), httpStatus.getReasonPhrase());
		}
		return null;
	}

	/**
	 * 打印请求详情
	 */
	private void printRequestDetail() {
		RequestDetail requestDetail = RequestDetailThreadLocal.getRequestDetail();
		if (requestDetail != null) {
			log.error("异常来源：ip: {}, path: {}", requestDetail.getIp(), requestDetail.getPath());
		}
	}
	/**
	 * 打印错误码及异常
	 *
	 * @param httpStatus
	 * @param exception
	 */
	private void printApiCodeException(HttpStatus httpStatus, Exception exception) {
		log.error(getApiCodeString(httpStatus), exception);
	}

}