package com.github.tix320.jouska.core.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.DoubleConsumer;

public class ChannelUtils {

	public static void pipe(ReadableByteChannel fromChannel, WritableByteChannel toChannel, long length,
							DoubleConsumer progressHandler) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024 * 64);

		long remaining = length;

		double lastProgress = 0;
		while (remaining != 0) {
			int read = fromChannel.read(buffer);
			if (read == -1) {
				throw new IOException("%s bytes remaining".formatted(remaining));
			}

			buffer.flip();
			toChannel.write(buffer);
			buffer.clear();
			remaining -= read;

			double progress = (length - remaining) / (double) length;
			final double diff = progress - lastProgress;
			if (diff > 0.01) { // run progress handler only for 1 percent up (for performance reasons)
				progressHandler.accept(progress);
				lastProgress = progress;
			}
		}

		if (lastProgress != 1.0) {
			progressHandler.accept(1.0);
		}
	}
}
