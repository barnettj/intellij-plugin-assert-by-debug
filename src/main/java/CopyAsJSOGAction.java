import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import dev.fervento.assertbydebug.BeanParser;
import dev.fervento.assertbydebug.CopyAssertByDebugAction;
import org.jetbrains.annotations.NotNull;

public class CopyAsJSOGAction extends AnAction {

    public CopyAsJSOGAction() {
        super("JSOG");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        CopyAssertByDebugAction.actionPerformed(event, BeanParser.CopyAs.JSOG);
    }

}
