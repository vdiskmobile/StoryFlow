package com.youqude.storyflow.exception;

public class StoryFlowException extends Exception{
    
    private int statusCode = -1;
    public static final int GeneralException = 8000;
    public StoryFlowException(String msg) {
        super(msg);
    }

    public StoryFlowException(Exception cause) {
        super(cause);
    }

    public StoryFlowException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

    public StoryFlowException(String msg, Exception cause) {
        super(msg, cause);
    }
    
    //此方法用来记录日志
    public StoryFlowException(String methodName, String msg, Exception cause, boolean log) {
        super(msg, cause);
        
        //记录下发生异常的方法名字， 异常信息， 异常， 发生时间
    }

    public StoryFlowException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
    
    public StoryFlowException() {
        super(); 
    }

    public StoryFlowException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public StoryFlowException(Throwable throwable) {
        super(throwable);
    }

    public StoryFlowException(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    
     public static class NoSignalException extends Exception {

            

            /**
             * 
             */
            private static final long serialVersionUID = -5385160439362548673L;

            public NoSignalException() {
                super();
            }

            public NoSignalException(String detailMessage, Throwable throwable) {
                super(detailMessage, throwable);
            }

            public NoSignalException(String detailMessage) {
                super(detailMessage);
            }

            public NoSignalException(Throwable throwable) {
                super(throwable);
            }

        }
        
        /**
         * @author sina
         *
         */
        public static class AccountException extends Exception {

          

            /**
             * 
             */
            private static final long serialVersionUID = 5818378371046691282L;

            public AccountException() {
                super();
            }

            public AccountException(String detailMessage, Throwable throwable) {
                super(detailMessage, throwable);
            }

            public AccountException(String detailMessage) {
                super(detailMessage);
            }

            public AccountException(Throwable throwable) {
                super(throwable);
            }

        }
        
        /**
         * @author sina
         *
         */
        public static class ServerException extends Exception {

            

            /**
             * 
             */
            private static final long serialVersionUID = -5028772314475863964L;

            public ServerException() {
                super();
            }

            public ServerException(String detailMessage, Throwable throwable) {
                super(detailMessage, throwable);
            }

            public ServerException(String detailMessage) {
                super(detailMessage);
            }

            public ServerException(Throwable throwable) {
                super(throwable);
            }

        }
        
        /**
         * @author sina
         *
         */
        public static class HttpException extends Exception {

            

            /**
             * 
             */
            private static final long serialVersionUID = -4386398761554445181L;
            
            private int statusCode = -1;

            public HttpException() {
                super();
            }

            public HttpException(String e) {
                super(e);
            }

            public HttpException(Throwable tr) {
                super(tr);
            }

            public HttpException(int statusCode) {
                super();
                this.statusCode = statusCode;
            }

            public int getStatusCode() {
                return statusCode;
            }
        }
}
