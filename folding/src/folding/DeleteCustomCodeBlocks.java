package folding;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class DeleteCustomCodeBlocks extends AbstractHandler {
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
		for (int i = annos.size() - 1; i >= 0; i--) {
			var anno = annos.get(i);
			annos.remove(anno);
			model.removeAnnotation(anno.anno);
		}
		return null;
	}
}