package folding;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;

public class DeleteCustomCodeBlockAtCursor extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		var pv = Utils.getProjectionViewerOfActiveEditor();
		if (pv == null) {
			return null;
		}
		var model = pv.getProjectionAnnotationModel();
		var annos = CollapseSelectedCodeBlock.ANNOTATIONS.get(model);
		if (annos == null) {
			return null;
		}
		var sel = pv.getSelectionProvider().getSelection();
		if (!(sel instanceof ITextSelection t)) {
			return null;
		}
		int offset = t.getOffset();
		for (int i = annos.size() - 1; i >= 0; i--) {
			var anno = annos.get(i);
			Position pos = model.getPosition(anno.anno);
			if (offset >= pos.getOffset() && offset < pos.getOffset() + pos.getLength()) {
				annos.remove(anno);
				model.removeAnnotation(anno.anno);
			}
		}
		return null;
	}
}