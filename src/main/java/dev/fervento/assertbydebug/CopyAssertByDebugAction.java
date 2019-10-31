package dev.fervento.assertbydebug;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CopyAssertByDebugAction {

    public static void actionPerformed(@NotNull AnActionEvent event, BeanParser.CopyAs copyAs) {
        Project project = event.getProject();

        ProcessHandler processHandler = XDebuggerManager.getInstance(project).getCurrentSession().getDebugProcess().getProcessHandler();
        if (processHandler != null) {
            BeanParser beanParser = new BeanParser(project, DebuggerManager.getInstance(project).getDebugProcess(processHandler), copyAs);

            List<XValueNodeImpl> selectedNode = XDebuggerTreeActionBase.getSelectedNodes(event.getDataContext());
            for (XValueNodeImpl xValueNode : selectedNode) {
                XValue valueContainer = xValueNode.getValueContainer();
                if (valueContainer instanceof JavaValue) {
                    beanParser.addValue((JavaValue) valueContainer);
                }
            }
            beanParser.submit();
        }
    }

}
