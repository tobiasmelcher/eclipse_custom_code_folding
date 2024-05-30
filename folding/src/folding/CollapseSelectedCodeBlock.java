package folding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.widgets.Display;

public class CollapseSelectedCodeBlock extends AbstractHandler {
	private static final String CUSTOM_CREATED_PROJECTION_TYPE = "folding.customCreatedProjection";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		var pv = Utils.getProjectionViewerOfActiveEditor();
		if (pv == null) {
			return null;
		}
		var sel = pv.getSelectionProvider().getSelection();
		if (!(sel instanceof ITextSelection t)) {
			return null;
		}
		var anno = new ProjectionAnnotation();
		anno.setType(CUSTOM_CREATED_PROJECTION_TYPE);
		var model = pv.getProjectionAnnotationModel();
		var doc = pv.getDocument();
		try {
			int startOffset = doc.getLineOffset(t.getStartLine());
			var pos = Utils.createPosition(doc, startOffset, t.getEndLine());
			model.addAnnotation(anno, pos);
			model.collapse(anno);
			model.addAnnotationModelListener(LISTENER);
			ANNOTATIONS.computeIfAbsent(model, k -> new ArrayList<>()).add(new Data(anno, pos, doc));
		} catch (BadLocationException e) {
		}
		return null;
	}

	static class Data {
		ProjectionAnnotation anno;
		Position initialPos;
		IDocument doc;

		public Data(ProjectionAnnotation anno, Position initialPos, IDocument doc) {
			this.anno = anno;
			this.initialPos = initialPos;
			this.doc = doc;
		}
	}

	static final Map<IAnnotationModel, List<Data>> ANNOTATIONS = new HashMap<>();
	private static final IAnnotationModelListener LISTENER = new IAnnotationModelListener() {

		@Override
		public void modelChanged(IAnnotationModel model) {
			if (!(model instanceof ProjectionAnnotationModel)) {
				return;
			}
			List<Data> annos = ANNOTATIONS.get(model);
			if (annos == null) {
				return;
			}
			List<Data> annotationsToBeAdded = new ArrayList<>();
			for (int i = annos.size() - 1; i >= 0; i--) {
				var data = annos.get(i);
				Position pos = model.getPosition(data.anno);
				if (pos == null) {
					boolean wasCollapsed = false;
					if (data.anno.isCollapsed()) {
						wasCollapsed = true;
					}
					data.anno = new ProjectionAnnotation();
					data.anno.setType(CUSTOM_CREATED_PROJECTION_TYPE);
					if (wasCollapsed) {
						data.anno.markCollapsed();
					}
					annotationsToBeAdded.add(data);
				}
			}
			if (annotationsToBeAdded.size() > 0) {
				addAnnotationsAsynchronously(model, annotationsToBeAdded);
			}
		}

		private void addAnnotationsAsynchronously(IAnnotationModel model, List<Data> annotationsToBeAdded) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					for (var data : annotationsToBeAdded) {
						model.addAnnotation(data.anno, data.initialPos);
					}
				}
			});
		}
	};
}