package org.cryptomator.frontend.fuse;

import org.cryptomator.jfuse.api.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;

@PerAdapter
class ReadOnlyLinkHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyLinkHandler.class);

	private final FileAttributesUtil attrUtil;
	private final FileNameTranscoder fileNameTranscoder;

	@Inject
	public ReadOnlyLinkHandler(FileAttributesUtil attrUtil, FileNameTranscoder fileNameTranscoder) {
		this.attrUtil = attrUtil;
		this.fileNameTranscoder = fileNameTranscoder;
	}

	public int getattr(Path path, BasicFileAttributes attrs, Stat stat) {
		if (attrs instanceof PosixFileAttributes) {
			PosixFileAttributes posixAttrs = (PosixFileAttributes) attrs;
			stat.setPermissions(posixAttrs.permissions());
		} else {
			stat.setMode(0555);
		}
		stat.toggleLnk(true);
		attrUtil.copyBasicFileAttributesFromNioToFuse(attrs, stat);
		return 0;
	}

	/**
	 * Writes the target of a link followed by a NUL byte to the given buffer.
	 * @param path the link to read
	 * @param buf buffer to which the link's target shall be written
	 * @param size maximum number of bytes to be written to the buffer including a NUL byte
	 * @return
	 * @throws IOException
	 */
	public int readlink(Path path, ByteBuffer buf, long size) throws IOException {
		Path target = Files.readSymbolicLink(path);
		ByteBuffer fuseEncodedTarget = fileNameTranscoder.interpretAsFuseString(fileNameTranscoder.nioToFuse(target.toString()));
		int len = (int) Math.min(fuseEncodedTarget.remaining(), size - 1);
		assert len < size;
		buf.put(0, fuseEncodedTarget.array(), 0, len);
		buf.put(len, (byte) 0x00); // add null terminator
		return 0;
	}


}
