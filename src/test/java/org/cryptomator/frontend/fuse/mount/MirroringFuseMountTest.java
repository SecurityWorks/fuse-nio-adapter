package org.cryptomator.frontend.fuse.mount;

import com.google.common.base.Preconditions;
import org.cryptomator.cryptofs.CryptoFileSystemProperties;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.cryptomator.cryptofs.DirStructure;
import org.cryptomator.cryptolib.common.MasterkeyFileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MirroringFuseMountTest {

	static {
		System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
		System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
		System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "HH:mm:ss.SSS");
	}

	private static final Logger LOG = LoggerFactory.getLogger(MirroringFuseMountTest.class);
	private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

	/**
	 * Mirror directory on Windows
	 */
	public static class WindowsMirror {

		public static void main(String[] args) {
			Preconditions.checkState(OS_NAME.contains("win"), "Test designed to run on Windows.");

			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("Enter path to the directory you want to mirror:");
				Path p = Paths.get(scanner.nextLine());
				System.out.println("Enter mount point:");
				Path m = Paths.get(scanner.nextLine());
				if (m.startsWith(p) || p.startsWith(m)) {
					LOG.error("Mirrored directory and mount location must not be nested.");
				} else if (Files.isDirectory(p)) {
					mount(p, m);
				} else {
					LOG.error("Invalid directory.");
				}
			}
		}
	}

	/**
	 * Mirror vault on Windows
	 */
	public static class WindowsCryptoFsMirror {

		public static void main(String args[]) throws IOException, NoSuchAlgorithmException {
			Preconditions.checkState(OS_NAME.contains("win"), "Test designed to run on Windows.");

			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("Enter path to the vault you want to mirror:");
				Path vaultPath = Paths.get(scanner.nextLine());
				Preconditions.checkArgument(CryptoFileSystemProvider.checkDirStructureForVault(vaultPath, "vault.cryptomator", "masterkey.cryptomator") == DirStructure.VAULT, "Not a vault: " + vaultPath);

				System.out.println("Enter vault password:");
				String passphrase = scanner.nextLine();

				SecureRandom csprng = SecureRandom.getInstanceStrong();
				CryptoFileSystemProperties props = CryptoFileSystemProperties.cryptoFileSystemProperties()
						.withKeyLoader(url -> new MasterkeyFileAccess(new byte[0], csprng).load(vaultPath.resolve("masterkey.cryptomator"), passphrase))
						.build();
				try (FileSystem cryptoFs = CryptoFileSystemProvider.newFileSystem(vaultPath, props)) {
					Path p = cryptoFs.getPath("/");
					System.out.println("Enter mount point:");
					Path m = Paths.get(scanner.nextLine());
					//Preconditions.checkArgument(Files.isDirectory(m), "Invalid mount point: " + m); //We don't need that on Windows
					LOG.info("Mounting FUSE file system at {}", m);
					mount(p, m);
				}
			}
		}

	}

	/**
	 * Mirror directory on Linux
	 */
	public static class LinuxMirror {

		public static void main(String args[]) {
			Preconditions.checkState(OS_NAME.contains("linux"), "Test designed to run on Linux.");

			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("Enter path to the directory you want to mirror:");
				Path p = Paths.get(scanner.nextLine());
				System.out.println("Enter mount point:");
				Path m = Paths.get(scanner.nextLine());
				if (m.startsWith(p) || p.startsWith(m)) {
					LOG.error("Mirrored directory and mount location must not be nested.");
				} else if (Files.isDirectory(p)) {
					mount(p, m);
				} else {
					LOG.error("Invalid directory.");
				}
			}
		}

	}

	/**
	 * Mirror vault on Linux
	 */
	public static class LinuxCryptoFsMirror {

		public static void main(String args[]) throws IOException, NoSuchAlgorithmException {
			Preconditions.checkState(OS_NAME.contains("linux"), "Test designed to run on Linux.");

			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("Enter path to the vault you want to mirror:");
				Path vaultPath = Paths.get(scanner.nextLine());
				Preconditions.checkArgument(CryptoFileSystemProvider.checkDirStructureForVault(vaultPath, "vault.cryptomator", "masterkey.cryptomator") == DirStructure.VAULT, "Not a vault: " + vaultPath);

				System.out.println("Enter vault password:");
				String passphrase = scanner.nextLine();

				SecureRandom csprng = SecureRandom.getInstanceStrong();
				CryptoFileSystemProperties props = CryptoFileSystemProperties.cryptoFileSystemProperties()
						.withKeyLoader(url -> new MasterkeyFileAccess(new byte[0], csprng).load(vaultPath.resolve("masterkey.cryptomator"), passphrase))
						.build();
				try (FileSystem cryptoFs = CryptoFileSystemProvider.newFileSystem(vaultPath, props)) {
					Path p = cryptoFs.getPath("/");
					System.out.println("Enter mount point:");
					Path m = Paths.get(scanner.nextLine());
					Preconditions.checkArgument(Files.isDirectory(m), "Invalid mount point: " + m);
					LOG.info("Mounting FUSE file system at {}", m);
					mount(p, m);
				}
			}
		}

	}

	/**
	 * Mirror directory on macOS
	 */
	public static class MacMirror {

		public static void main(String args[]) {
			Preconditions.checkState(OS_NAME.contains("mac"), "Test designed to run on macOS.");

			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("Enter path to the directory you want to mirror:");
				Path p = Paths.get(scanner.nextLine());
				Path m = Paths.get("/Volumes/" + UUID.randomUUID().toString());
				LOG.info("Mounting FUSE file system at {}", m);
				mount(p, m);
			}
		}

	}

	/**
	 * Mirror vault on macOS
	 */
	public static class MacCryptoFsMirror {

		public static void main(String args[]) throws IOException, NoSuchAlgorithmException {
			Preconditions.checkState(OS_NAME.contains("mac"), "Test designed to run on macOS.");

			try (Scanner scanner = new Scanner(System.in)) {
				System.out.println("Enter path to the vault you want to mirror:");
				Path vaultPath = Paths.get(scanner.nextLine());
				Preconditions.checkArgument(CryptoFileSystemProvider.checkDirStructureForVault(vaultPath, "vault.cryptomator", "masterkey.cryptomator") == DirStructure.VAULT, "Not a vault: " + vaultPath);

				System.out.println("Enter vault password:");
				String passphrase = scanner.nextLine();

				SecureRandom csprng = SecureRandom.getInstanceStrong();
				CryptoFileSystemProperties props = CryptoFileSystemProperties.cryptoFileSystemProperties()
						.withKeyLoader(url -> new MasterkeyFileAccess(new byte[0], csprng).load(vaultPath.resolve("masterkey.cryptomator"), passphrase))
						.build();
				try (FileSystem cryptoFs = CryptoFileSystemProvider.newFileSystem(vaultPath, props)) {
					Path p = cryptoFs.getPath("/");
					Path m = Paths.get("/Volumes/" + UUID.randomUUID().toString());
					LOG.info("Mounting FUSE file system at {}", m);
					mount(p, m);
				}
			}
		}

	}

	private static void mount(Path pathToMirror, Path mountPoint) {
		Mounter mounter = FuseMountFactory.getMounter();
		EnvironmentVariables envVars = EnvironmentVariables.create()
				.withFlags(mounter.defaultMountFlags())
				.withMountPoint(mountPoint)
				.withFileNameTranscoder(mounter.defaultFileNameTranscoder())
				.build();
		CountDownLatch barrier = new CountDownLatch(1);
		Consumer<Throwable> onFuseMainExit = throwable -> barrier.countDown();
		try (Mount mnt = mounter.mount(pathToMirror, envVars, onFuseMainExit, false)) {
			LOG.info("Mounted successfully. Enter anything to stop the server...");
			try {
				mnt.reveal(new AwtFrameworkRevealer());
			} catch (Exception e) {
				LOG.warn("Reveal failed.", e);
			}
			System.in.read();
			try {
				mnt.unmountGracefully();
			} catch (FuseMountException e) {
				LOG.info("Unable to perform regular unmount.", e);
				LOG.info("Forcing unmount...");
			}
		} catch (IOException | FuseMountException e) {
			LOG.error("Mount failed", e);
		}

		try {
			if (!barrier.await(5000, TimeUnit.MILLISECONDS)) {
				LOG.error("Wait on onFuseExit action to finish exceeded timeout. Exiting ...");
			} else {
				LOG.info("onExit action executed.");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.error("Main thread interrupted. Exiting without waiting for onFuseExit action");
		}
	}

	private static boolean isVaultDir(Path vaultPath) throws IOException {
		return CryptoFileSystemProvider.checkDirStructureForVault(vaultPath, "vault.cryptomator", "masterkey.cryptomator") == DirStructure.VAULT;
	}
}

