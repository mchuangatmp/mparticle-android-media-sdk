package com.mparticle.media.internal

import android.util.Log
import com.mparticle.MParticle
import com.mparticle.MParticle.LogLevel

object Logger {
    private val LOG_TAG = "mParticle"
    private val DEFAULT_MIN_LOG_LEVEL = LogLevel.DEBUG
    private var sMinLogLevel = DEFAULT_MIN_LOG_LEVEL
    private var sExplicitlySet = false
    private var logHandler: AbstractLogHandler =
        DefaultLogHandler()

    fun setMinLogLevel(minLogLevel: LogLevel) {
        setMinLogLevel(minLogLevel, null)
    }

    fun setMinLogLevel(minLogLevel: LogLevel, explicit: Boolean?) {
        if (explicit != null) {
            sExplicitlySet = explicit
        }
        if (sExplicitlySet && explicit == null) {
            return
        }
        sMinLogLevel = minLogLevel
    }

    fun getMinLogLevel(): LogLevel {
        return sMinLogLevel
    }


    fun verbose(vararg messages: String) {
        verbose(null, *messages)
    }

    fun verbose(error: Throwable?, vararg messages: String) {
        getLogHandler()
            .log(LogLevel.VERBOSE, error, getMessage(*messages))
    }

    fun info(vararg messages: String) {
        info(null, *messages)
    }

    fun info(error: Throwable?, vararg messages: String) {
        getLogHandler().log(MParticle.LogLevel.INFO, error,
            getMessage(*messages)
        )
    }


    fun debug(vararg messages: String) {
        debug(null, *messages)
    }

    fun debug(error: Throwable?, vararg messages: String) {
        getLogHandler().log(MParticle.LogLevel.DEBUG, error,
            getMessage(*messages)
        )
    }


    fun warning(vararg messages: String) {
        warning(null, *messages)
    }

    fun warning(error: Throwable?, vararg messages: String) {
        getLogHandler().log(MParticle.LogLevel.WARNING, error,
            getMessage(*messages)
        )
    }


    fun error(vararg messages: String) {
        error(null, *messages)
    }

    fun error(error: Throwable?, vararg messages: String) {
        getLogHandler().log(MParticle.LogLevel.ERROR, error,
            getMessage(*messages)
        )
    }

    private fun getMessage(vararg messages: String): String {
        val logMessage = StringBuilder()
        for (m in messages) {
            logMessage.append(m)
        }
        return logMessage.toString()
    }


    /**
     * Testing method. Use this method to intercept Logs, or customize what happens when something is logged.
     * For example, you can use this method to throw an exception every time an "error" log is called.
     * @param logListener
     */
    fun setLogHandler(logListener: AbstractLogHandler?) {
        logHandler = logListener ?: DefaultLogHandler()
    }

    fun getLogHandler(): AbstractLogHandler {
        return logHandler
    }

    abstract class AbstractLogHandler {

        open fun log(priority: MParticle.LogLevel, error: Throwable?, messages: String?) {
            if (messages != null && isLoggable(priority.logLevel)) {
                when (priority) {
                    MParticle.LogLevel.ERROR -> error(error, messages)
                    MParticle.LogLevel.WARNING -> warning(error, messages)
                    MParticle.LogLevel.DEBUG -> debug(error, messages)
                    MParticle.LogLevel.VERBOSE -> verbose(error, messages)
                    MParticle.LogLevel.INFO -> info(error, messages)
                    else -> info(error, messages)
                }
            }
        }

        private fun isLoggable(logLevel: Int): Boolean {
            val isAPILoggable = logLevel >= sMinLogLevel.logLevel
            val isADBLoggable: Boolean

            //This block will catch the exception that is thrown during testing.
            try {
                isADBLoggable = isADBLoggable(LOG_TAG, logLevel)
            } catch (ex: UnsatisfiedLinkError) {
                return false
            } catch (ignored: RuntimeException) {
                return false
            }

            return isADBLoggable || isAPILoggable && MParticle.getInstance()?.environment == MParticle.Environment.Development
        }

        //Override this method during testing, otherwise this will throw an error and logs will not be printed.
        protected fun isADBLoggable(tag: String, logLevel: Int): Boolean {
            return Log.isLoggable(tag, logLevel)
        }

        internal abstract fun verbose(error: Throwable?, message: String)
        internal abstract fun info(error: Throwable?, message: String)
        internal abstract fun debug(error: Throwable?, message: String)
        internal abstract fun warning(error: Throwable?, message: String)
        internal abstract fun error(error: Throwable?, message: String)
    }

    open class DefaultLogHandler : AbstractLogHandler() {

        public override fun verbose(error: Throwable?, message: String) {
            if (error != null) {
                Log.v(LOG_TAG, message, error)
            } else {
                Log.v(LOG_TAG, message)
            }
        }

        public override fun info(error: Throwable?, message: String) {
            if (error != null) {
                Log.i(LOG_TAG, message, error)
            } else {
                Log.i(LOG_TAG, message)
            }
        }

        public override fun debug(error: Throwable?, message: String) {
            if (error != null) {
                Log.d(LOG_TAG, message, error)
            } else {
                Log.d(LOG_TAG, message)
            }
        }

        public override fun warning(error: Throwable?, message: String) {
            if (error != null) {
                Log.w(LOG_TAG, message, error)
            } else {
                Log.w(LOG_TAG, message)
            }
        }

        public override fun error(error: Throwable?, message: String) {
            if (error != null) {
                Log.e(LOG_TAG, message, error)
            } else {
                Log.e(LOG_TAG, message)
            }
        }
    }

}