package com.chugyoyo.cosmosagent.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HTTP Status Codes
 * 参考：https://cloud.tencent.com/developer/article/2540361
 */
@AllArgsConstructor
@Getter
public enum HttpStatus {

    // 1xx Informational - 信息性状态码
    /** 100 Continue: 客户端应当继续发送请求 */
    CONTINUE(100, "Continue"),
    /** 101 Switching Protocols: 服务器已经理解了客户端的请求，并将通过Upgrade消息头通知客户端采用不同的协议来完成这个请求 */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    /** 102 Processing: 处理将被继续执行 */
    PROCESSING(102, "Processing"),
    /** 103 Early Hints: 用来表示在最终响应之前，服务器已经准备了一些提示信息 */
    EARLY_HINTS(103, "Early Hints"),

    // 2xx Success - 成功状态码
    /** 200 OK: 请求已成功，请求所希望的响应头或数据体将随此响应返回 */
    OK(200, "OK"),
    /** 201 Created: 请求已经被实现，而且有一个新的资源已经依据请求的需要而建立 */
    CREATED(201, "Created"),
    /** 202 Accepted: 服务器已接受请求，但尚未处理 */
    ACCEPTED(202, "Accepted"),
    /** 203 Non-Authoritative Information: 请求已成功处理，但返回的元信息不在原始的服务器上 */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    /** 204 No Content: 服务器成功处理了请求，但不需要返回任何实体内容 */
    NO_CONTENT(204, "No Content"),
    /** 205 Reset Content: 服务器成功处理了请求，且没有返回任何内容，但要求客户端重置文档视图 */
    RESET_CONTENT(205, "Reset Content"),
    /** 206 Partial Content: 服务器已经成功处理了部分GET请求 */
    PARTIAL_CONTENT(206, "Partial Content"),
    /** 207 Multi-Status: 代表之后的消息体将是一个XML消息，并且可能包含一系列单独的响应代码 */
    MULTI_STATUS(207, "Multi-Status"),
    /** 208 Already Reported: DAV绑定成员已经被之前在同一个集合请求中的操作所枚举 */
    ALREADY_REPORTED(208, "Already Reported"),
    /** 226 IM Used: 服务器已经成功处理了请求，且返回的是文档的当前实例的副本 */
    IM_USED(226, "IM Used"),

    // 3xx Redirection - 重定向状态码
    /** 300 Multiple Choices: 被请求的资源有一系列可供选择的回馈信息 */
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    /** 301 Moved Permanently: 请求的资源已被永久移动到新的URL */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /** 302 Found: 请求的资源临时被移动到了新的URL */
    FOUND(302, "Found"),
    /** 303 See Other: 服务器建议客户端访问另一个URL以获取响应 */
    SEE_OTHER(303, "See Other"),
    /** 304 Not Modified: 如果客户端发送了一个带条件的GET请求且该请求已被允许，而文档的内容并没有改变 */
    NOT_MODIFIED(304, "Not Modified"),
    /** 305 Use Proxy: 被请求的资源必须通过指定的代理才能被访问 */
    USE_PROXY(305, "Use Proxy"),
    /** 307 Temporary Redirect: 请求的资源临时从不同URI响应 */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /** 308 Permanent Redirect: 资源已被永久移动到Location头指定的URL */
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    // 4xx Client Error - 客户端错误状态码
    /** 400 Bad Request: 由于语法错误，服务器无法理解请求 */
    BAD_REQUEST(400, "Bad Request"),
    /** 401 Unauthorized: 请求未经授权 */
    UNAUTHORIZED(401, "Unauthorized"),
    /** 402 Payment Required: 保留此状态码为将来需求 */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /** 403 Forbidden: 服务器拒绝执行请求 */
    FORBIDDEN(403, "Forbidden"),
    /** 404 Not Found: 服务器无法找到请求的资源 */
    NOT_FOUND(404, "Not Found"),
    /** 405 Method Not Allowed: 请求中指定的方法不被允许 */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /** 406 Not Acceptable: 无法根据客户端请求的内容特性完成请求 */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /** 407 Proxy Authentication Required: 请求要求代理的身份验证 */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /** 408 Request Timeout: 服务器等待客户端发送请求时间过长 */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /** 409 Conflict: 请求的资源与当前状态冲突，请求无法完成 */
    CONFLICT(409, "Conflict"),
    /** 410 Gone: 请求的资源已永久不可用，无已知转发地址 */
    GONE(410, "Gone"),
    /** 411 Length Required: 服务器拒绝未定义Content-Length头的请求 */
    LENGTH_REQUIRED(411, "Length Required"),
    /** 412 Precondition Failed: 服务器未满足请求中的前提条件 */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /** 413 Payload Too Large: 请求实体过大，超出服务器处理能力 */
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    /** 414 URI Too Long: 请求的URI过长，服务器无法处理 */
    URI_TOO_LONG(414, "URI Too Long"),
    /** 415 Unsupported Media Type: 请求的格式不受支持 */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /** 416 Requested Range Not Satisfiable: 客户端请求的范围无效 */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    /** 417 Expectation Failed: 服务器无法满足Expect头指定的期望值 */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /** 418 I'm a teapot: 茶壶不支持冲泡咖啡，用于愚人节玩笑 */
    IM_A_TEAPOT(418, "I'm a teapot"),
    /** 421 Misdirected Request: 请求被定向到无法处理的服务器，常见于HTTP/2 */
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    /** 422 Unprocessable Entity: 请求格式正确，但含语义错误无法处理 */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    /** 423 Locked: 当前资源被锁定 */
    LOCKED(423, "Locked"),
    /** 424 Failed Dependency: 由于之前的某个请求发生的错误，导致当前请求失败 */
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    /** 425 Too Early: 服务器不愿意冒着风险来处理该请求 */
    TOO_EARLY(425, "Too Early"),
    /** 426 Upgrade Required: 客户端应该切换到TLS/1.0 */
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    /** 428 Precondition Required: 服务器要求请求是条件请求 */
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    /** 429 Too Many Requests: 在给定的时间内发送了太多请求 */
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    /** 431 Request Header Fields Too Large: 请求头字段太大 */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    /** 451 Unavailable For Legal Reasons: 由于法律原因，资源不可用 */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    // 5xx Server Error - 服务器错误状态码
    /** 500 Internal Server Error: 服务器内部错误，无法完成请求 */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /** 501 Not Implemented: 服务器不支持请求的功能，无法完成请求 */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /** 502 Bad Gateway: 作为网关或者代理工作的服务器尝试执行请求时，从上游服务器接收到无效的响应 */
    BAD_GATEWAY(502, "Bad Gateway"),
    /** 503 Service Unavailable: 服务器当前无法处理请求 */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /** 504 Gateway Timeout: 作为网关或者代理工作的服务器尝试执行请求时，未能及时从上游服务器接收响应 */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /** 505 HTTP Version Not Supported: 服务器不支持请求的HTTP协议版本 */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    /** 506 Variant Also Negotiates: 服务器存在内部配置错误 */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    /** 507 Insufficient Storage: 服务器无法存储完成请求所必须的内容 */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    /** 508 Loop Detected: 服务器在处理请求时检测到无限循环 */
    LOOP_DETECTED(508, "Loop Detected"),
    /** 510 Not Extended: 服务器需要对请求进行扩展 */
    NOT_EXTENDED(510, "Not Extended"),
    /** 511 Network Authentication Required: 客户端需要进行网络认证 */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    private final int code;
    private final String message;
}
