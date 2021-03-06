// This file is part of CyberShadow's SWF tools.
// Some code is based on or derived from the Adobe Flex SDK, and redistribution is subject to the SDK License.

package net.thecybershadow.swf.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.thecybershadow.swf.tools.doabclayout.Layout;
import net.thecybershadow.swf.tools.doabclayout.Slot;
import net.thecybershadow.swf.tools.doabclayout.Tag;
import flash.swf.TagDecoder;
import flash.swf.TagHandler;
import flash.swf.tags.DoABC;
import flash.util.FileUtils;

public class AbcExport extends TagHandler
{
	public static void main(String[] args) throws IOException, JAXBException
	{
		if (args.length != 1)
		{
			System.err.println("Exports DoABC tags from a SWF to individual .abc files.");
			System.err.println("Usage: AbcExport input.swf > abclayout.xml");
			return;
		}

		File file = new File(args[0]);
		URL url = FileUtils.toURL(file);
		AbcExport abcdump = new AbcExport(args[0].substring(0, args[0].lastIndexOf(".")));
		InputStream in = url.openStream();
		new TagDecoder(in, url).parse(abcdump);
		abcdump.finish();

		JAXBContext jc = JAXBContext.newInstance("net.thecybershadow.swf.tools.doabclayout");
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(abcdump.xmlLayout, System.out);
	}

	int counter = 0;
	String prefix;
	Layout xmlLayout = new Layout();

	public AbcExport(String prefix)
	{
		this.prefix = prefix;
	}

	@Override
	public void doABC(DoABC tag)
	{
		try
		{
			String filename = prefix + counter + ".abc";
			//System.out.println("Writing DoABC tag \"" + tag.name + "\" with flag=" + tag.flag + " to " + filename);
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(tag.abc);
			fos.close();

			Tag xmlTag = new Tag();
			xmlTag.setFilename(filename);
			xmlTag.setName(tag.name);
			xmlTag.setFlag(tag.flag);

			Slot xmlSlot = new Slot();
			xmlSlot.getTag().add(xmlTag);
			xmlLayout.getSlot().add(xmlSlot);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		counter++;
	}

	static String changeExtension(String originalName, String newExtension)
	{
		int lastDot = originalName.lastIndexOf(".");
		if (lastDot != -1)
			return originalName.substring(0, lastDot) + newExtension;
		else
			return originalName + newExtension;
	}
}
