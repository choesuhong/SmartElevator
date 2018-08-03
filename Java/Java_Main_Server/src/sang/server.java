package sang;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

public class server extends JPanel {
	BufferedImage image; // �̹����� ������ �̹��� ����.
	static Connection conn = null; // DataBase�� ������ ���� �������� ����.
	static Statement stmt = null; // DataBase�� Query �̿� ����.
	static ResultSet rs = null; // DataBase�� Query �̿� ����.

	public static void main(String[] args) throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
					"jdbc:mysql://ssdb.ccijo8xfuwup.ap-northeast-2.rds.amazonaws.com:3306/SSDB?verifyServerCertificate=false&useSSL=true",
					"username", "password"); // DataBase�� ����-> RDS ����ϱ�.
			System.out.println("=====================================================================================================================================================================================");
			ShowData(); // ������ ���̽��� ����Ǿ� �ִ� ����� ������ �����ش�.
			System.out.println("=====================================================================================================================================================================================\n\n");
			
			ServerSocket serverARDUINO = new ServerSocket(10001);
			System.out.println("<  ���������� �۵��κ�[�Ƶ��̳�] ���� �����   >");
			Socket sockARDUINO = serverARDUINO.accept();
			InetAddress inetaddrARDUINO = sockARDUINO.getInetAddress();
			System.out.println("< ���������� �۵�[�Ƶ��̳�] ���� > IP :" + inetaddrARDUINO.getHostAddress() + "\n\n");
			/*
			 * �Ƶ��̳� �����ϱ� ���� ��Ʈ�� ����.
			 */
			OutputStream outARDUINO = sockARDUINO.getOutputStream();
			PrintWriter pwARDUINO = new PrintWriter(new OutputStreamWriter(outARDUINO));

			while (true) {
				
				ServerSocket serverANDROID = new ServerSocket(10000);
				System.out.println("<  ���������� �۵��κ�[�ȵ���̵�] ���� �����   >");
				Socket sockANDROID = serverANDROID.accept();
				InetAddress inetaddrANDROID = sockANDROID.getInetAddress();
				System.out.println("< ���������� �۵�[�ȵ���̵�] ���� > IP :" + inetaddrANDROID.getHostAddress() + "\n\n");
				/*
				 * �ȵ���̵� �����ϱ� ���� ��Ʈ�� ����.
				 */
				OutputStream outANDROID = sockANDROID.getOutputStream();
				PrintWriter pwANDROID = new PrintWriter(new OutputStreamWriter(outANDROID));
				BufferedReader inANDROID = new BufferedReader(new InputStreamReader(sockANDROID.getInputStream()));

				
				String tranString = new String();
				ArrayList<Integer> array = new ArrayList<Integer>(); // ������ ����� ���� ����Ʈ.			
				
				String inputButton = new String(); // �̹������Ͽ� �̸��� �޴´�.
				try {

					while ((inputButton = inANDROID.readLine()) != null) {
						
						if(inputButton.compareTo("U")==0)
						{
							tranString = new String(); // �ʱ�ȭ.
							array = new ArrayList<Integer>(); // �ʱ�ȭ.

							System.out.println("�ȵ���̵���� ���� ��ư : " + inputButton);

							String tempImage = new String();
							while (tempImage.isEmpty() == true) {
								tempImage = Take_Picture(); // ���� ������ ��´�. ����� ���� ��θ� ������
							}

							// ��ü �̹����� -> ���̹�����
							int FaceNumber = pictureTOface("C:\\Users\\ice305\\Desktop\\server\\");

							System.out.println("���������Ϳ��� ���� ������ ����� �� : " + FaceNumber);	

							for (int i = 0; i < FaceNumber; i++) {
								String tempString = new String();
								tempString = SelectElevatorFloor(
										"C:\\Users\\ice305\\Desktop\\server\\detectingFace" + (i + 1) + ".png");
								System.out.println("�νĵ� "+(i + 1) + "��° ��� ����  : " + tempString);
								if (!tempString.isEmpty()) // ��ȯ ���� null �ƴҶ� : ������ ����� ������!!!
								{
									tranString += (tempString + "F"); // �ȵ���̵忡�� �����ֱ� ���� ��Ʈ��.
									int tempNumber = Integer.parseInt(tempString);
									array.add(tempNumber);
								}
							}
							if (array.size() == 0) // �νĵ� ���� �ϳ��� ������.
							{
								continue;
							} else {
								pwANDROID.println(tranString);
								pwANDROID.flush();
								System.out.println("---------------------------------------------------\n\n\n");
							}
						}
						else if(inputButton.compareTo("C")==0)
						{
							int transArray[] = convertIntegers(array); // ��Ʈ�� �ϱ� ���ؼ� �迭 ��ȯ.
							java.util.Arrays.sort(transArray); // ������� �����ϱ� ���� �����Ѵ�.
							for (int i = 0; i < transArray.length; i++) {
								pwARDUINO.println(transArray[i]);
								System.out.println("�Ƶ��̳� ������ �� : " + transArray[i]);
								pwARDUINO.flush();
							}
							tranString = new String(); //�����鼭 �ʱ�ȭ.
							array = new ArrayList<Integer>(); //�����鼭 �ʱ�ȭ.
							
						}
						else
						{
							int temp = Integer.parseInt(inputButton);
							if(array.contains(temp))
							{
								for (int i = 0; i < array.size(); i++) {
									if(array.get(i)==temp)
									{
										array.remove(i);
									}
								}
								System.out.println(array.toString());
							}
							else{
								array.add(temp);
								System.out.println(array.toString());
							}
						}

					}

				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				sockANDROID.close();
				// ois.close();
				serverANDROID.close();
				System.out.println("< ���������� �ȵ���̵� ���� >");
			}
		} catch (ClassNotFoundException cnfe) {
			System.out.println("The class could not be found." + cnfe.getMessage());
		} catch (SQLException se) {
			System.out.println(se.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public server() {
	}

	public server(BufferedImage img) {
		image = img;
	}

	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}

	public static void ShowData() throws SQLException {
		stmt = (Statement) conn.createStatement();
		rs = stmt.executeQuery("select * from info");
		if (rs == null)
			System.out.println("No such content found.");
		System.out.println("name   \t\tphoneNumber\troomNumber\tEntranceTime\t\t\t\tExitTime\t\t\tpicture");
		while (rs.next()) {
			String name = rs.getString("name");
			String phoneNumber = rs.getString("phoneNumber");
			String roomNumber = rs.getString("roomNumber");
			String EntranceTime = rs.getString("EntranceTime");
			String ExitTime = rs.getString("ExitTime");
			String picture = rs.getString("picture");
			System.out.printf("%s\t%s\t%s\t\t%s\t\t%s\t%s", name, phoneNumber, roomNumber, EntranceTime, ExitTime,
					picture);
			System.out.println("");
		}
	}

	public static String SelectElevatorFloor(String address) throws SQLException { //��񿡼� �ش� ������ ���� ������ ��ȯ.
		String floor = new String();
		int result = 0;
		stmt = (Statement) conn.createStatement();
		rs = stmt.executeQuery("select * from info");
		if (rs == null)
			System.out.println("No such content found.");

		while (rs.next()) {
			String name = rs.getString("name");
			int tempNumber = ComparePicture(address, "C:\\Users\\ice305\\Desktop\\Customerphotos\\" + name + ".png");
			if (tempNumber > result) {
				result = tempNumber; // ���� �������� ū���� ã�ƾߵȴ�.
				floor = rs.getString("level"); // ���� �������� ū���� ����(�̸�)�� ����.
			}
		}
		System.out.println("");

		return floor;
	}

	public static String Take_Picture() { // ������� ������ ����.
		String result = new String(); // ���������� ����!!
		System.loadLibrary("opencv_java2412"); // ���̺귯�� �ε�.
		server t = new server(); // ĸ���̹��� ��ü �������ش�.
		VideoCapture camera = new VideoCapture(0); // ������ ĸ���� ī�޶� ��ü�� �������ش�.
		Mat frame = new Mat(); // ����� �������ش�
		if (!camera.isOpened()) { // ī�޶� ����������.
			System.out.println("ī�޶� ���� �����ϴ�.");
		} else {
			while (true) {
				if (camera.read(frame)) { // ī�޶󿡼� �̹��� �����͸� ��Ĵ����� �о� �����ϸ�.
					BufferedImage image = t.MatToBufferedImage(frame); // ����̹���
																		// �����͸�
																		// �̹���
																		// �����ͷ�.
					t.grayscale(image);
					t.saveImage(image);
					result = "C:\\\\\\\\Users\\\\\\\\ice305\\\\\\\\Desktop\\\\\\\\server\\\\\\\\PicturesTakenFromElevator.png";
					break;
				}
			}
		}
		camera.release();
		return result; // ���������� ��θ� �����Ѵ�.
	}

	public static int pictureTOface(String address) { //��ü������ �󱼻����� �����ؼ� ����
		int detectingFace = 0; // �������� ���� � �������� ������.
		System.loadLibrary("opencv_java2412"); // ���̺귯�� �ε�.
		CascadeClassifier faceDetector = new CascadeClassifier(
				"C:\\Users\\ice305\\Downloads\\opencv\\build\\share\\OpenCV\\lbpcascades\\lbpcascade_frontalface.xml");
		int imageNumber = 0;
		while (true) {
			Mat Mat_image = Highgui.imread(address + "\\PicturesTakenFromElevator.png");
			// �̹������� �󱼰���
			MatOfRect faceDetections = new MatOfRect();
			faceDetector.detectMultiScale(Mat_image, faceDetections);
			detectingFace = faceDetections.toArray().length;
			Mat temp = new Mat();
			// �׸��׷��ش� �׸�ڽ�!!
			for (Rect rect : faceDetections.toArray()) {
				temp = new Mat(Mat_image, rect);
				imageNumber++; // ���ص� ������ŭ �����ϱ� ���ؼ�. -> ī���Ϳ����� �ʿ����.
				String filename = "detectingFace" + imageNumber + ".png";
				Highgui.imwrite("C:\\Users\\ice305\\Desktop\\server\\" + filename, temp);
			}
			break;
		}
		return detectingFace; // ����� ���� ���� �����Ѵ�.
	}

	// �̹����� ������� �����ش�.
	public void window(BufferedImage img, String text, int x, int y) {
		JFrame frame0 = new JFrame();
		frame0.getContentPane().add(new server(img));
		frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame0.setTitle(text); // tool action bar �̸� �����ִ°�.
		frame0.setSize(img.getWidth(), img.getHeight() + 30); // tool ũ��.
		frame0.setLocation(x, y);// ������� ��� ��ġ���ִ���.
		frame0.setVisible(true); // false ���ָ� ������� �ȳ��´�.
	}

	// Load an image �̹����� �ε��Ѵ�.
	public BufferedImage loadImage(String file) {
		BufferedImage img;
		try {
			File input = new File(file);
			img = ImageIO.read(input);
			return img;
		} catch (Exception e) {
			System.out.println("erro");
		}

		return null;
	}

	// �̹��� �����͸� window�� �����Ѵ�.
	public void saveImage(BufferedImage img) {
		try {
			File outputfile = new File("C:\\Users\\ice305\\Desktop\\server\\PicturesTakenFromElevator.png");
			ImageIO.write(img, "png", outputfile);
		} catch (Exception e) {
			System.out.println("error");
		}
	}

	// ȸ������ ����.
	public BufferedImage grayscale(BufferedImage img) {
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				Color c = new Color(img.getRGB(j, i));

				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);

				Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);

				img.setRGB(j, i, newColor.getRGB());
			}
		}

		return img;
	}

	// ��ĵ����͸� �޾Ƽ� �̹��� �������������� �ٲ��ִ� �Լ�.
	public BufferedImage MatToBufferedImage(Mat frame) {
		// Mat() to BufferedImage
		int type = 0;
		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);

		return image;
	}

	public static int ComparePicture(String address1, String address2) { //�� �̹��� �񱳺м�
		int resultRatio = 0;

		System.loadLibrary("opencv_java2412");

		String picture = address1; // �񱳴��
		String basePicture = address2; // �񱳱���

		// System.out.println("\n\n\n\n------------���α׷� ����-------------");
		// System.out.println("1. �̹��� �ε����");

		Mat objectPictureImage = Highgui.imread(picture, Highgui.CV_LOAD_IMAGE_COLOR); // �̹�����
																						// ��ķ�
																						// �����.
		Mat objectBasePictureImage = Highgui.imread(basePicture, Highgui.CV_LOAD_IMAGE_COLOR); // �̹�����
																								// ��ķ�
																								// �����.

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint(); // ��Ŀ� Ű����Ʈ�� ������ ��ü
																// ����.
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SURF);// Ư¡��
																						// �˰���
																						// ��ü����.

		// System.out.println("2. �񱳴�� �̹��� Ű����Ʈ ����(����̿�)"); //Ű����Ʈ ���� �ǽ�!!
		featureDetector.detect(objectPictureImage, objectKeyPoints);

		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		descriptorExtractor.compute(objectPictureImage, objectKeyPoints, objectDescriptors);

		// ���̹����� �����̹����� �� �ǽ�.
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
		// System.out.println("4. ���� �̹����� �ֿ� ���� ����");
		featureDetector.detect(objectBasePictureImage, sceneKeyPoints);
		// System.out.println("5. ���� �̹����� ������ ���");
		descriptorExtractor.compute(objectBasePictureImage, sceneKeyPoints, sceneDescriptors);

		List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
		DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		// System.out.println("6. �񱳴�� �� ���� �̹��� ��Ī��Ű��");
		descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

		// System.out.println("7. ��Ī ���");
		LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

		float nndrRatio = 0.7f;

		for (int i = 0; i < matches.size(); i++) {
			MatOfDMatch matofDMatch = matches.get(i);
			DMatch[] dmatcharray = matofDMatch.toArray();
			DMatch m1 = dmatcharray[0];
			DMatch m2 = dmatcharray[1];

			if (m1.distance <= m2.distance * nndrRatio) {
				goodMatchesList.addLast(m1);
			}
		}
		System.out.println("������ : " + goodMatchesList.size()); // �󸶳� ����Ѱ��� �ִ���
																// ���!!

		if (goodMatchesList.size() >= 10) {
			// System.out.println(" (�����ι�)Object Found");
			resultRatio = goodMatchesList.size();
			// Mat img = Highgui.imread(basePicture,
			// Highgui.CV_LOAD_IMAGE_COLOR);
			// System.out.println("8. �̹����� ��ġ�ϴ� �׸� �׸���");
			// MatOfDMatch goodMatches = new MatOfDMatch();
			// goodMatches.fromList(goodMatchesList);
			//
			// Highgui.imwrite("C:\\Users\\rtr45\\Desktop\\image\\sang5.png",
			// img);
		} else {
			// System.out.println("�ٸ��ι�(Object Not Found)");
		}
		// System.out.println("-------------���α׷� ����-------------");

		return resultRatio;
	}
}