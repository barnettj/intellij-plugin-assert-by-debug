import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.CopyAssertByDebugAction;
import org.jetbrains.annotations.NotNull;

public class CopyAsJUnitAssertionsAction extends AnAction {

    public CopyAsJUnitAssertionsAction() {
        super("JUnit assertions");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        CopyAssertByDebugAction.actionPerformed(event, BeanParser.CopyAs.JUNIT);
    }

}
