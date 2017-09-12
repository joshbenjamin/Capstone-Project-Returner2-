///*
//import java.io.IOException;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.rendering.PDFRenderer;
//import java.awt.image.BufferedImage;
//import javax.imageio.ImageIO;
//import java.io.File;
//import org.apache.pdfbox.multipdf.Splitter;
//import java.util.List;
//import java.util.Iterator;
//
//public class PDF2Image {
//	public static void main(String[] args) throws IOException{
//
//		int pageCount = 7; // Sets the number of pages in this test (will have to get from Test class)
//
//		File pdfFile = new File("C:\\Users\\Joshua\\Desktop\\TempMaven\\Files\\StudentCoverPage1.pdf"); // Gets the pdf file which is locally stored (will be passed through Vula)
//
//		PDDocument document = PDDocument.load(pdfFile);
//		PDFRenderer renderer = new PDFRenderer(document);
//		BufferedImage image;
//
//		Splitter splitter = new Splitter();
//
//		List<PDDocument> Pages = splitter.split(document); // Splits the PDF document into each page
//
//		Iterator<PDDocument> iterator = Pages.listIterator();
//
//		int curPageNo = 1; // Used to track which page we're on
//
//		PDDocument newPDF = new PDDocument();
//
//		while(iterator.hasNext()){
//
//			PDDocument current = iterator.next(); // Makes each page a completely new PDF file
//
//			if((curPageNo%pageCount)==1){ // Checks if the page is a cover page (we have to add the scanning for front page here)
//
//				newPDF = new PDDocument(); // Makes this a new PDF file (for each Student)
//
//				image = renderer.renderImage(curPageNo-1); // Converts that one PDF page to an image
//
//				ImageIO.write(image, "PNG", new File("C:\\Users\\Joshua\\Desktop\\TempMaven\\Files\\StudentCoverPage" + (int)(Math.ceil(curPageNo/pageCount)+1) + ".png"));
//				// Writes the image to disk
//			}
//
//			newPDF.addPage(current.getPage(0)); // Adds each PDF page to the batch that is the Student's test
//
//			if((curPageNo%pageCount)==0){ // Checks to see if this is the last page of the test
//
//				newPDF.save("C:\\Users\\Joshua\\Desktop\\TempMaven\\Files\\StudentTest" + (int)Math.ceil(curPageNo/pageCount) + ".pdf"); // Saves the Student's PDF file
//				newPDF.close();
//			}
//
//			curPageNo++;
//
//		}
//
//		newPDF.close();
//		document.close();  // Closing of PDF documents so there's no memory leak
//		Pages.clear();
//	}
//}
//*/
