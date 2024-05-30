package folding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class DocumentSetupParticipant implements IDocumentSetupParticipant {
	private static final String PROJECTION_ANNO_TYPE_FOR_REGION = "folding.regionProjection";

	@Override
	public void setup(IDocument document) {
		Display.getDefault().asyncExec(() -> onSetup(document));
	}

	private static ProjectionViewer getProjectionViewer(IDocument document) {
		for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (var page : window.getPages()) {
				for (var editorRef : page.getEditorReferences()) {
					IEditorPart ed = editorRef.getEditor(false);
					if (ed != null) {
						var pv = Utils.getProjectionViewer(ed);
						if (pv != null) {
							var doc = pv.getDocument();
							if (doc == document) {
								return pv;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private void onSetup(IDocument document) {
		var pv = getProjectionViewer(document);
		if (pv == null) {
			return;
		}
		document.addDocumentListener(listener);
		calculateFoldingForRegions(document, pv);
	}

	private static void calculateFoldingForRegions(IDocument doc, ProjectionViewer pv) {
		if (doc == null || pv == null) {
			return;
		}
		String source = doc.get();
		List<Integer> regionIdxList = new ArrayList<>();
		List<Position> positions = new ArrayList<>();
		int ind = 0;
		while (true) {
			int regionIdx = source.indexOf("#region", ind);
			int endregionIdx = source.indexOf("#endregion", ind);
			if (regionIdx > 0 && (regionIdx < endregionIdx || endregionIdx < 0)) {
				regionIdxList.add(regionIdx);
				ind = regionIdx + 1;
				continue;
			} else if (endregionIdx > 0 && (endregionIdx < regionIdx || regionIdx < 0)) {
				ind = endregionIdx + 1;
				if (regionIdxList.size() > 0) {
					int start = regionIdxList.get(regionIdxList.size() - 1);
					regionIdxList.remove(regionIdxList.size() - 1);
					int end = endregionIdx;
					try {
						int endLine = doc.getLineOfOffset(end);
						var pos = Utils.createPosition(doc, start, endLine);
						positions.add(pos);
					} catch (BadLocationException e) { // silently ignored
					}
				}
				continue;
			}
			break;
		}
		var model = pv.getProjectionAnnotationModel();
		if (model==null) {
			return;
		}
		var it = model.getAnnotationIterator();
		List<ProjectionAnnotation> annosToBeRemoved = new ArrayList<>();
		while (it.hasNext()) {
			var anno = it.next();
			if (anno instanceof ProjectionAnnotation pa) {
				var pos = model.getPosition(anno);
				if (positions.contains(pos)) {
					positions.remove(pos);
				} else {
					var annoType = anno.getType();
					if (PROJECTION_ANNO_TYPE_FOR_REGION.equals(annoType)) {
						annosToBeRemoved.add(pa);
					}
				}
			}
		}
		for (var anno : annosToBeRemoved) {
			model.removeAnnotation(anno);
		}
		for (var pos : positions) {
			var anno = new ProjectionAnnotation();
			anno.setType(PROJECTION_ANNO_TYPE_FOR_REGION);
			model.addAnnotation(anno, pos);
		}
	}

	private static final IDocumentListener listener = new IDocumentListener() {
		Runnable last;
		IDocument lastDoc;

		@Override
		public void documentChanged(DocumentEvent event) {
			var doc = event.getDocument();
			if (doc == null) {
				return;
			}
			if (lastDoc != doc) {
				lastDoc = doc;
				last = new Runnable() {

					@Override
					public void run() {
						calculateFoldingForRegions(doc, getProjectionViewer(doc));
					}
				};
			}
			Display.getDefault().timerExec(1000, last);
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	};
}
