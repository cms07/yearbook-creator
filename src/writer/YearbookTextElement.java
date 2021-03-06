package writer;
import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

/**
 * A textual element
 * @author Cody Crow
 *
 */
public class YearbookTextElement extends YearbookElement implements Serializable {
	private static final long serialVersionUID = 6972127757271364075L;
	
	
	public String text;
	public String fontFamily;
	public int size; //In points
	public boolean bold;
	public boolean italic;
	public boolean underline;
	public boolean shadow;
	public TextElementAlign align;
	
	private RGB rgb;
	transient private Font font;
	transient private Color color;
	public int pageWidth;
	public int pageHeight;
	
	private double width;
	private double height;

	@Override
	public YearbookElement copy() {
		YearbookTextElement copy = new YearbookTextElement(this.pageWidth, this.pageHeight);
		copy.x = this.x;
		copy.y = this.y;
		copy.rotation = this.rotation;
		copy.text = this.text;
		copy.fontFamily = this.fontFamily;
		copy.size = this.size;
		copy.bold = this.bold;
		copy.italic = this.italic;
		copy.underline = this.underline;
		copy.shadow = this.shadow;
		copy.rgb = this.rgb;
		copy.width = this.width;
		copy.height = this.height;
		copy.border = this.border;
		return copy;
	}

	public YearbookTextElement(int pageWidth, int pageHeight) {
		generateRandomElementId();
		this.x = 0;
		this.y = 0;
		this.rotation = 0;
		this.rgb = new RGB(0, 0, 0);
		this.text = "";
		this.size = 7;
		this.fontFamily = "Arial";
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.align = TextElementAlign.LEFT;
		this.border = new YearbookElementBorder();
	}
	
	public YearbookTextElement(int x, int y, int pageWidth, int pageHeight) {
		this(pageWidth, pageHeight);
		this.x = ((double) x / pageWidth);
		this.y = ((double) y / pageHeight);
	}
	
	@Override
	public Rectangle getBounds() {
		int x = (int) (this.x * pageWidth);
		int y = (int) (this.y * pageHeight);
		int width = (int) (this.width * pageWidth);
		int height = (int) (this.height * pageHeight);
		return new Rectangle(x, y, width, height);
	}
	@Override
	public Rectangle getBounds(int pageWidth, int pageHeight) {
		int x = (int) (this.x * pageWidth);
		int y = (int) (this.y * pageHeight);
		int width = (int) (this.width * pageWidth);
		int height = (int) (this.height * pageHeight);
		return new Rectangle(x, y, width, height);
	}
	
	/**
	 * This merely informs the text element of its bounds. It does NOT
	 * force the text to conform to it.
	 * @param r the new bounds
	 */
	public void setBounds(Rectangle r) {
		this.x = (double) r.x / pageWidth;
		this.y = (double) r.y / pageHeight;
		this.width = (double) r.width / pageWidth;
		this.height = (double) r.height / pageHeight;
	}
	
	@Override
	boolean isAtPoint(int x, int y) {
		return this.getBounds().contains(x, y);
	}
	@Override
	boolean isAtPoint(int x, int y, int pageWidth, int pageHeight) {
		return this.getBounds(pageWidth, pageHeight).contains(x, y);
	}
	@Override
	public void setLocationRelative(int x, int y) {
		this.x = (double) x / this.pageWidth;
		this.y = (double) y / this.pageHeight;
		
	}
	
	public void setYRelative(int y, int pageHeight) {
		this.y = (double) y / pageHeight;
	}
	
	@Override
	public void resize(Display display, int x, int y) {
		// TODO Auto-generated method stub
		
	}
	
	public Color getColor(Device device) {
		if (color != null) color.dispose();
		color = new Color(device, rgb);
		return color;
	}
	
	public Font getFont(Device d) {
		if (font != null) font.dispose();
		font = new Font(d, this.fontFamily, this.size, this.fontStyle());
		return font;
	}
	
	public Font getFont(Device d, int pageWidth, int pageHeight) {
		if (font != null) font.dispose();
		
		int size = (int) (this.size * ((double) pageWidth / this.pageWidth));
		
		font = new Font(d, this.fontFamily, size, this.fontStyle());
		return font;
	}
	
	public int fontStyle() {
		int style = SWT.NORMAL;
		if (bold) style |= SWT.BOLD;
		if (italic) style |= SWT.ITALIC;
		return style;
	}

	public boolean isText() {
		return true;
	}
	
	public void setRGB(RGB rgb) {
		this.rgb = rgb;
	}
	
	public void toggleBold() {
		this.bold = !this.bold;
	}
	
	public void toggleItalic() {
		this.italic = !this.italic;
	}
	
	public void toggleUnderline() {
		this.underline = !this.underline;
	}
	
	public void toggleShadow() {
		this.shadow = !this.shadow;
	}

	public RGB getRgb() {
		return rgb;
	}
	
	public void draw(GC gc, int x, int y) {
		TextLayout tl = new TextLayout(gc.getDevice());
		tl.setText(this.text);
		tl.setWidth(gc.textExtent(this.text).x > 0 ? gc.textExtent(this.text).x : 5);
		System.out.println(tl.getWidth());
		switch (this.align) {
		case JUSTIFY:
			tl.setJustify(true);
			tl.setAlignment(SWT.LEFT);
			break;
		case LEFT:
			tl.setAlignment(SWT.LEFT);
			tl.setJustify(false);
			break;
		case CENTER:
			tl.setAlignment(SWT.CENTER);
			tl.setJustify(false);
			break;
		case RIGHT:
			tl.setAlignment(SWT.RIGHT);
			tl.setJustify(false);
			break;
		}
		tl.setFont(this.getFont(gc.getDevice()));
		//if (font != null) tl.setFont(this.font);
		
		//TextStyle ts = new TextStyle();
		//ts.underlineColor = this.getColor(gc.getDevice());
		//ts.underlineStyle = SWT.UNDERLINE_SINGLE;
		//if (text.length() > 0) tl.setStyle(ts, 0, this.text.length() - 1);
		
		tl.draw(gc, x, y);
		tl.dispose();
	}

	@Override
	public void dispose() {
		font.dispose();
		color.dispose();
		
	}
}
