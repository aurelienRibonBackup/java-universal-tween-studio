package aurelienribon.tweenstudio.ui.timeline;

import aurelienribon.tweenstudio.ui.timeline.TimelineModel.Element;
import aurelienribon.tweenstudio.ui.timeline.TimelineModel.ElementAction;
import aurelienribon.tweenstudio.ui.timeline.gfx.ResourcesHelper;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class NamesPanel extends JPanel {
	private final BufferedImage imgIdxClosed = ResourcesHelper.getGfx("img_idxClosed.png");
	private final BufferedImage imgIdxOpened = ResourcesHelper.getGfx("img_idxOpened.png");
	private final BufferedImage imgIdxNone = ResourcesHelper.getGfx("img_idxNone.png");
	private final int paddingTop = 30;
	private final int paddingLeft = 10;
	private final int paddingIncremental = 30;
	private final int lineHeight = 20;
	
	private TimelineModel model;
	private Element selectedElement;
	private Element mouseOverElement;

	public NamesPanel() {
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	public void setModel(TimelineModel model) {
		this.model = model;
	}

	public void setSelectedElement(Element selectedElement) {
		this.selectedElement = selectedElement;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D gg = (Graphics2D) g;
		gg.setColor(Theme.COLOR_NAMESPANEL_BACKGROUND);
		gg.fillRect(0, 0, getWidth(), getHeight());
		if (model == null) return;

		gg.translate(0, paddingTop);
		drawSections(gg);
		drawNames(gg);
	}

	private void drawSections(final Graphics2D gg) {
		model.forAllElements(new TimelineModel.ElementAction() {
			private int line = 0;
			@Override public boolean apply(Element elem) {
				if (!elem.isSelectable()) {
					gg.setColor(Theme.COLOR_NAMESPANEL_SECTION_UNUSABLE);
				} else if (elem == selectedElement) {
					gg.setColor(Theme.COLOR_NAMESPANEL_SECTION_SELECTED);
				} else if (elem == mouseOverElement) {
					gg.setColor(Theme.COLOR_NAMESPANEL_SECTION_MOUSEOVER);
				} else {
					gg.setColor(Theme.COLOR_NAMESPANEL_SECTION);
				}

				gg.fillRect(0, line*lineHeight, getWidth(), lineHeight);
				line += 1;
				return false;
			}
		});
	}

	private void drawNames(final Graphics2D gg) {
		gg.setFont(Theme.FONT);
		gg.setColor(Theme.COLOR_FOREGROUND);

		model.forAllElements(new TimelineModel.ElementAction() {
			private int line = 0;
			@Override public boolean apply(Element elem) {
				int level = model.getPath(elem).length;
				int x = paddingLeft + (level-1)*paddingIncremental;
				int y = line*lineHeight-5;
				Image img = elem.getChildren().isEmpty() ? imgIdxNone : imgIdxClosed;

				gg.drawImage(img, x, y + 7, null);
				gg.drawString(elem.getName(), x + 20, y + 20);

				line += 1;
				return false;
			}
		});
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private int getLineFromY(int y) {
		if (y < paddingTop) return -1;
		return (y - paddingTop) / lineHeight;
	}

	// -------------------------------------------------------------------------
	// Input
	// -------------------------------------------------------------------------

	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (selectedElement != mouseOverElement) {
				selectedElement = mouseOverElement;
				fireSelectedElementChanged(selectedElement);
			}
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mouseOverElement = null;
			final int evLine = getLineFromY(e.getY());

			model.forAllElements(new ElementAction() {
				private int line = 0;
				@Override public boolean apply(Element elem) {
					if (evLine == line && elem.isSelectable()) {
						mouseOverElement = elem;
						return true;
					}

					line += 1;
					return false;
				}
			});
			
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseOverElement = null;
			repaint();
		}
	};

	// -------------------------------------------------------------------------
	// Events
	// -------------------------------------------------------------------------

	private final List<EventListener> listeners = new ArrayList<EventListener>();
	public void addListener(EventListener listener) {listeners.add(listener);}

	public interface EventListener {
		public void selectedElementChanged(Element selectedElem);
	}

	private void fireSelectedElementChanged(Element selectedElem) {
		for (EventListener listener : listeners)
			listener.selectedElementChanged(selectedElem);
	}
}