package org.cryptomator.frontend.fuse;

import org.cryptomator.jfuse.api.Errno;

import java.nio.file.Path;

public class AdapterFactory {

	/**
	 * The default value for the maximum supported filename length.
	 */
	public static final int DEFAULT_MAX_FILENAMELENGTH = 254; // 255 is preferred, but nautilus checks for this value + 1

	private AdapterFactory() {
	}

	/**
	 * Creates a read-only fuse-nio filesystem with a maximum file name length of {@value DEFAULT_MAX_FILENAMELENGTH} and an assumed filename encoding of UTF-8 NFC for FUSE and the NIO filesystem.
	 *
	 * @param root the root path of the NIO filesystem.
	 * @return an adapter mapping FUSE callbacks to the nio interface
	 * @see ReadOnlyAdapter
	 * @see FileNameTranscoder
	 */
	public static FuseNioAdapter createReadOnlyAdapter(Path root, Errno errno) {
		return createReadOnlyAdapter(root, errno, DEFAULT_MAX_FILENAMELENGTH, FileNameTranscoder.transcoder());
	}

	public static FuseNioAdapter createReadOnlyAdapter(Path root, Errno errno, int maxFileNameLength, FileNameTranscoder fileNameTranscoder) {
		FuseNioAdapterComponent comp = DaggerFuseNioAdapterComponent.builder()
				.root(root)
				.errno(errno)
				.maxFileNameLength(maxFileNameLength)
				.fileNameTranscoder(fileNameTranscoder)
				.build();
		return comp.readOnlyAdapter();
	}

	/**
	 * Creates a fuse-nio-filesystem with a maximum file name length of {@value DEFAULT_MAX_FILENAMELENGTH} and an assumed filename encoding of UTF-8 NFC for FUSE and the NIO filesystem.
	 *
	 * @param root the root path of the NIO filesystem.
	 * @return an adapter mapping FUSE callbacks to the nio interface
	 * @see ReadWriteAdapter
	 * @see FileNameTranscoder
	 */
	public static FuseNioAdapter createReadWriteAdapter(Path root, Errno errno) {
		return createReadWriteAdapter(root, errno, DEFAULT_MAX_FILENAMELENGTH);
	}

	/**
	 * Creates a fuse-nio-filesystem with an assumed filename encoding of UTF-8 NFC for FUSE and the NIO filesystem.
	 *
	 * @param root the root path of the NIO filesystem.
	 * @return an adapter mapping FUSE callbacks to the nio interface
	 * @see ReadWriteAdapter
	 * @see FileNameTranscoder
	 */
	public static FuseNioAdapter createReadWriteAdapter(Path root, Errno errno, int maxFileNameLength) {
		return createReadWriteAdapter(root, errno, maxFileNameLength, FileNameTranscoder.transcoder());
	}

	public static FuseNioAdapter createReadWriteAdapter(Path root, Errno errno, int maxFileNameLength, FileNameTranscoder fileNameTranscoder) {
		FuseNioAdapterComponent comp = DaggerFuseNioAdapterComponent.builder()
				.root(root)
				.errno(errno)
				.maxFileNameLength(maxFileNameLength)
				.fileNameTranscoder(fileNameTranscoder)
				.build();
		return comp.readWriteAdapter();
	}
}
