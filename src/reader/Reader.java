package reader;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import writer.Creator;
import writer.SWTUtils;
import writer.Yearbook;
import writer.YearbookClickableElement;
import writer.YearbookClickableImageElement;
import writer.YearbookElement;
import writer.YearbookImageElement;
import writer.YearbookTextElement;

/**
 * Displays a yearbook to the end user.
 * @author Cody Crow
 *
 */
public class Reader {
	Display display;
	Shell shell;
	
	Yearbook yearbook;

	private Canvas canvas;
	private Canvas rightCanvas;
	private Color canvasBackgroundColor;
	
	public Reader() throws ClassNotFoundException, IOException {
		display = new Display();
		shell = new Shell(display);
		/*
		 * Actual end-user program will not have a dialog.
		 */
		FileDialog picker = new FileDialog(shell, SWT.OPEN);
		picker.setText("Open Yearbook");
		picker.setFilterExtensions(new String[] {"*.ctc"});
		String fileName = picker.open();
		if (fileName == null) return;
		
		
		this.initialize();
		yearbook = Yearbook.readFromDisk(fileName);
		this.createNewYearbook();
		
		this.refresh();
		
		shell.setLayout(new FillLayout());
		shell.setText(Creator.COMPANY_NAME + " Digital Yearbook");
		shell.pack();
		//Magic number, chosen for being near center.
		shell.setLocation((int) (.09375 * display.getClientArea().width), 0);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();
	}
	
	private void initialize() {
		canvasBackgroundColor = new Color(display, 254, 254, 254);
		
		Composite bigCanvasWrapper = new Composite(shell, SWT.NONE);
		bigCanvasWrapper.setLayout(new GridLayout(2, false));
		
		Composite canvasWrapper = new Composite(bigCanvasWrapper, SWT.NONE);
		canvas = new Canvas(canvasWrapper, SWT.BORDER);
		canvas.setBackground(canvasBackgroundColor);
		
		Composite canvasWrapper2 = new Composite(bigCanvasWrapper, SWT.NONE);
		rightCanvas = new Canvas(canvasWrapper2, SWT.BORDER);
		rightCanvas.setBackground(canvasBackgroundColor);
		
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				yearbook.activePage -= 2;
				if (yearbook.activePage < 0) yearbook.activePage = 0;
				refresh();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (rightIsActive() && yearbook.activePage - 1 >= 0) yearbook.activePage--;
				refresh();
				
				if (yearbook.page(yearbook.activePage).isClickableAtPoint(e.x, e.y) && leftIsActive()) {
					//Show their video.
					//Using the system player for now.
					File file;
					if (yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y).isImage()) {
						file = new File(((YearbookClickableImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
					} else {
						file = new File(((YearbookClickableElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
					}
					
					Desktop dt = Desktop.getDesktop();
					
					try {
						dt.open(file);
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						MessageBox box = new MessageBox(shell, SWT.ERROR);
						box.setText("Error");
						box.setMessage("The video was not loaded successfully.");
						box.open();
					}
				}
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				
			}
			
		});
		
		rightCanvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				yearbook.activePage += 2;
				if (yearbook.activePage >= yearbook.size()) yearbook.activePage = yearbook.size() - 1;
				refresh();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (leftIsActive() && yearbook.activePage + 1 < yearbook.size()) yearbook.activePage++;
				refresh();

				if (yearbook.page(yearbook.activePage).isClickableAtPoint(e.x, e.y) && rightIsActive()) {
					//Show their video.
					//Using the system player for now.
					File file;
					if (yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y).isImage()) {
						file = new File(((YearbookClickableImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
					} else {
						file = new File(((YearbookClickableElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
					}
					
					Desktop dt = Desktop.getDesktop();
					
					try {
						dt.open(file);
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						MessageBox box = new MessageBox(shell, SWT.ERROR);
						box.setText("Error");
						box.setMessage("The video was not loaded successfully.");
						box.open();
					}
				} 
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				
			}
			
		});
	}

	private void createNewYearbook() {

		int canvasHeight = display.getClientArea().height - 80;
		
		yearbook.settings.height = canvasHeight;
		yearbook.settings.width = (int) ((8.5 / 11.0) * canvasHeight);
		canvas.setSize(yearbook.settings.width, yearbook.settings.height);
		rightCanvas.setSize(yearbook.settings.width, yearbook.settings.height);
		
		yearbook.activePage = 0;
		
	}
	
	private void loadPages(int pageIndex) {
		
		//Back cover
		if (pageIndex + 1 == yearbook.size() && leftIsActive()) {
			blankRightCanvas();
			loadLeftCanvas(pageIndex);
			return;
		}
	
		//Front cover
		if (pageIndex == 0) {
			blankLeftCanvas();
			loadRightCanvas(0);
			return;
		} 
		
		//Active page is odd
		if (leftIsActive()) {
			loadLeftCanvas(pageIndex);
			loadRightCanvas(pageIndex + 1);
			return;
		}
		
		//Active page is even
		if (rightIsActive()) {
			loadLeftCanvas(pageIndex - 1);
			loadRightCanvas(pageIndex);
			return;
		}
		
		
	}
	
	private boolean leftIsActive() {
		return Math.abs(yearbook.activePage % 2) == 1;
	}
	
	private boolean rightIsActive() {
		return yearbook.activePage % 2 == 0; 
	}
	
	private void blankLeftCanvas() {
		GC gc;
		gc = new GC(canvas);
		gc.setBackground(canvasBackgroundColor);
		gc.fillRectangle(0, 0, canvas.getBounds().width, canvas.getBounds().height);
		gc.dispose();		
	}
	
	private void blankRightCanvas() {
		GC gc;
		gc = new GC(rightCanvas);
		gc.setBackground(canvasBackgroundColor);
		gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		gc.dispose();		
	}
	
	private void loadLeftCanvas(int activePage) {
		blankLeftCanvas();
		GC gc;
		
		if (yearbook.page(activePage).backgroundImage(display) != null) {
			gc = new GC(canvas);
			gc.drawImage(yearbook.page(activePage).backgroundImage(display), 0, 0, yearbook.page(activePage).backgroundImage(display).getBounds().width, yearbook.page(activePage).backgroundImage(display).getBounds().height, 0, 0, canvas.getBounds().width, canvas.getBounds().height);
			gc.dispose();
		}
		
		//Apparently there's no map function in Java.
		//Map the YearbookImageElements to images...
		ArrayList<YearbookImageElement> images = new ArrayList<YearbookImageElement>();
		for (int i = 0; i < yearbook.page(activePage).getElements().size(); i++) {
			if (yearbook.page(activePage).element(i).isImage()) {
				images.add((YearbookImageElement) yearbook.page(activePage).element(i));
			}
		}
		//...and display them.
		for (YearbookImageElement element : images) {
			gc = new GC(canvas);
			gc.drawImage(element.getImage(display), 0, 0, element.getImage(display).getBounds().width, element.getImage(display).getBounds().height, element.getBounds(yearbook.settings.width, yearbook.settings.height).x, element.getBounds(yearbook.settings.width, yearbook.settings.height).y, element.getBounds(yearbook.settings.width, yearbook.settings.height).width, element.getBounds(yearbook.settings.width, yearbook.settings.height).height);
			gc.dispose();
		}
		

		//Next, draw the text elements.
		ArrayList<YearbookTextElement> texts = new ArrayList<YearbookTextElement>();
		for (YearbookElement e : yearbook.page(activePage).getElements()) {
			if (e.isText()) texts.add((YearbookTextElement) e);
		}
		
		//...and display those in some manner.
		for (YearbookTextElement e : texts) {
			gc = new GC(canvas);
			gc.setTextAntialias(SWT.ON);
			gc.setForeground(e.getColor(display));
			gc.setFont(e.getFont(display));
			gc.drawText(e.text, e.getBounds(yearbook.settings.width, yearbook.settings.height).x, e.getBounds(yearbook.settings.width, yearbook.settings.height).y, true);

			/*
			 * Inform the text element of its bounds.
			 * This must be done here, regrettably.
			 */
			//e.setBounds(new Rectangle(e.getBounds(yearbook.settings.width, yearbook.settings.height).x, e.getBounds(yearbook.settings.width, yearbook.settings.height).y, gc.stringExtent(e.text).x, gc.stringExtent(e.text).y));
			
			/*
			 * Handle underlining (SWT has no native GC underlining)
			 * All magic numbers were chosen for their looks.
			 */
			if (e.underline) {
				//Determine the line width
				int width;
				width = e.size / 12;
				if (width <= 0) width = 1;
				
				if (e.bold) width *= 1.8;
				gc.setLineWidth(width);
				gc.drawLine(e.getBounds(yearbook.settings.width, yearbook.settings.height).x + 1, e.getBounds(yearbook.settings.width, yearbook.settings.height).y + e.getBounds(yearbook.settings.width, yearbook.settings.height).height - (int) (e.getBounds(yearbook.settings.width, yearbook.settings.height).height * .1), e.getBounds(yearbook.settings.width, yearbook.settings.height).x + e.getBounds(yearbook.settings.width, yearbook.settings.height).width - 1, e.getBounds(yearbook.settings.width, yearbook.settings.height).y + e.getBounds(yearbook.settings.width, yearbook.settings.height).height - (int) (e.getBounds(yearbook.settings.width, yearbook.settings.height).height * .1));
				
			}
			
			gc.dispose();
			
		}
		
		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (!yearbook.page(activePage).isElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
					return;
				}
				
				if (yearbook.page(activePage).isClickableAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
				} else {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
				}
			}
			
		});
		
	}
	
	private void loadRightCanvas(int activePage) {
		blankRightCanvas();
		GC gc;
		
		if (yearbook.page(activePage).backgroundImage(display) != null) {
			gc = new GC(rightCanvas);
			gc.drawImage(yearbook.page(activePage).backgroundImage(display), 0, 0, yearbook.page(activePage).backgroundImage(display).getBounds().width, yearbook.page(activePage).backgroundImage(display).getBounds().height, 0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
			gc.dispose();
		}
		
		//Apparently there's no map function in Java.
		//Map the YearbookImageElements to images...
		ArrayList<YearbookImageElement> images = new ArrayList<YearbookImageElement>();
		for (int i = 0; i < yearbook.page(activePage).getElements().size(); i++) {
			if (yearbook.page(activePage).element(i).isImage()) {
				images.add((YearbookImageElement) yearbook.page(activePage).element(i));
			}
		}
		//...and display them.
		for (YearbookImageElement element : images) {
			gc = new GC(rightCanvas);
			gc.drawImage(element.getImage(display), 0, 0, element.getImage(display).getBounds().width, element.getImage(display).getBounds().height, element.getBounds(yearbook.settings.width, yearbook.settings.height).x, element.getBounds(yearbook.settings.width, yearbook.settings.height).y, element.getBounds(yearbook.settings.width, yearbook.settings.height).width, element.getBounds(yearbook.settings.width, yearbook.settings.height).height);
			gc.dispose();
		}
		
		//Next, draw the text elements.
		ArrayList<YearbookTextElement> texts = new ArrayList<YearbookTextElement>();
		for (YearbookElement e : yearbook.page(activePage).getElements()) {
			if (e.isText()) texts.add((YearbookTextElement) e);
		}
		
		//...and display those in some manner.
		for (YearbookTextElement e : texts) {
			gc = new GC(rightCanvas);
			gc.setTextAntialias(SWT.ON);
			gc.setForeground(e.getColor(display));
			gc.setFont(e.getFont(display));
			gc.drawText(e.text, e.getBounds(yearbook.settings.width, yearbook.settings.height).x, e.getBounds(yearbook.settings.width, yearbook.settings.height).y, true);

			/*
			 * Inform the text element of its bounds.
			 * This must be done here, regrettably.
			 */
			//e.setBounds(new Rectangle(e.getBounds(yearbook.settings.width, yearbook.settings.height).x, e.getBounds(yearbook.settings.width, yearbook.settings.height).y, gc.stringExtent(e.text).x, gc.stringExtent(e.text).y));
			
			/*
			 * Handle underlining (SWT has no native GC underlining)
			 * All magic numbers were chosen for their looks.
			 */
			if (e.underline) {
				//Determine the line width
				int width;
				width = e.size / 12;
				if (width <= 0) width = 1;
				
				if (e.bold) width *= 1.8;
				gc.setLineWidth(width);
				gc.drawLine(e.getBounds(yearbook.settings.width, yearbook.settings.height).x + 1, e.getBounds(yearbook.settings.width, yearbook.settings.height).y + e.getBounds(yearbook.settings.width, yearbook.settings.height).height - (int) (e.getBounds(yearbook.settings.width, yearbook.settings.height).height * .1), e.getBounds(yearbook.settings.width, yearbook.settings.height).x + e.getBounds(yearbook.settings.width, yearbook.settings.height).width - 1, e.getBounds(yearbook.settings.width, yearbook.settings.height).y + e.getBounds(yearbook.settings.width, yearbook.settings.height).height - (int) (e.getBounds(yearbook.settings.width, yearbook.settings.height).height * .1));
				
			}
			
			gc.dispose();
			
		}
		
		rightCanvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (!yearbook.page(activePage).isElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
					return;
				}
				
				if (yearbook.page(activePage).isClickableAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
				} else {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
				}
			}
			
		});
		
	}
	
	private void refresh() {
		loadPages(yearbook.activePage);
		shell.layout();
	}

	public static void main(String[] args) {
		try {
			new Reader();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
