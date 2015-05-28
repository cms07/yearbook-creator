package writer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class YearbookPage implements Serializable {
	private static final long serialVersionUID = -5090460491486388571L;
	private ArrayList<YearbookElement> elements;
	public String name;
	transient private ImageData backgroundImageData;
	transient private Image backgroundImage;
	boolean noBackground;
	
	public YearbookPage(Image backgroundImage) {
		this();
		this.name = "";
		this.backgroundImageData = backgroundImage.getImageData();
	}

	public YearbookElement element(int index) {
		return elements.get(index);
	}
	
	public ArrayList<YearbookElement> getElements() {
		return elements;
	}

	public YearbookPage() {
		elements = new ArrayList<YearbookElement>();
	}

	public YearbookPage(String name) {
		this();
		this.name = name;
	}
	
	public void addElement(YearbookElement e) {
		elements.add(e);
	}
	
	public Image backgroundImage(Display display) {
		//Try not to leak too many resources...
		if (display == null || this.backgroundImageData == null) return null; 
		if (this.backgroundImage == null) {
			this.backgroundImage = new Image(display, this.backgroundImageData);
		}
		return this.backgroundImage;
	}
	
	public String toString() {
		return this.name;
	}
	
	public boolean isElementAtPoint(int x, int y) {
		for (YearbookElement e : elements) {
			if (e.isAtPoint(x, y)) return true;
		}
		return false;
	}
	
	public YearbookElement getElementAtPoint(int x, int y) {
		for (int i = elements.size() - 1; i >= 0; i--) {
			YearbookElement e = elements.get(i);
			if (e.isAtPoint(x, y)) return e;
		}
		return null;
	}

	/**
	 * Finds the element on the page which is equal to e
	 * @param e the YearbookElement to compare to
	 * @return the found YearbookElement on the page
	 */
	public YearbookElement findElement(YearbookElement e) {
		for (int i = 0; i < this.elements.size(); i++) {
			if (this.elements.get(i) == e) {
				return this.elements.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Finds the element on the page which is equal to e
	 * @param e the YearbookElement to compare to
	 * @return the found YearbookElement on the page
	 */
	public int findElementIndex(YearbookElement e) {
		for (int i = 0; i < this.elements.size(); i++) {
			if (this.elements.get(i) == e) {
				return i;
			}
		}
		return -1;
	}
	
	/*
	 * Serialization methods
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		if (this.backgroundImageData == null) {
			this.noBackground = true;
			this.backgroundImageData = YearbookImages.bogusBackgroundData();
		} else {
			this.noBackground = false;
		}
		out.defaultWriteObject();
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { this.backgroundImageData };
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageLoader.save(stream, SWT.IMAGE_PNG);
		byte[] bytes = stream.toByteArray();
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		int length = in.readInt();
		byte[] buffer = new byte[length];
		in.readFully(buffer);
		ImageLoader imageLoader = new ImageLoader();
		ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
		ImageData[] data = imageLoader.load(stream);
		this.backgroundImageData = data[0];
		if (this.noBackground == true) {
			this.backgroundImageData = null;
		}
	}
}
