package folding;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class Utils {

	public static Position createPosition(IDocument doc, int startOffset, int endLine) throws BadLocationException {
		int endOffset;
		if (endLine + 1 == doc.getNumberOfLines()) {
			endOffset = doc.getLength();
		} else {
			endOffset = doc.getLineOffset(endLine + 1);
		}
		var pos = new Position(startOffset, endOffset - startOffset);
		return pos;
	}
	
	public static ProjectionViewer getProjectionViewer(IWorkbenchPart editor) {
		if (editor instanceof MultiPageEditorPart mpe) {
			var page = mpe.getSelectedPage();
			if (page instanceof IEditorPart) {
				editor = (IEditorPart) page;
			}
		}
		if (!(editor instanceof ITextEditor && editor instanceof WorkbenchPart part)) {
			return null;
		}
		ITextViewer viewer = part.getAdapter(ITextViewer.class);
		if (!(viewer instanceof ProjectionViewer pv)) {
			return null;
		}
		return pv;
	}
	
	public static ProjectionViewer getProjectionViewerOfActiveEditor() {
		var editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		return getProjectionViewer(editor);
	}
}
