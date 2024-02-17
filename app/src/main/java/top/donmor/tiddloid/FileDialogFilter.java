/*
 * top.donmor.tiddloid.FileDialogFilter <= [P|Tiddloid]
 * Last modified: 22:01:34 2024/02/15
 * Copyright (c) 2024 donmor
 */

package top.donmor.tiddloid;

/**
 * The File type filter for FileDialogOpen.
 */
class FileDialogFilter {
	/**
	 * The extension names related.
	 */
	private final String[] extensions;

	private static final String ALL = "*";

	/**
	 * Instantiates a new File dialog filter.
	 *
	 * @param extensions The extensions array. It should be formatted like { ".extension1", ".extension2", ... }. The extension can be like ".ext1.ext2". Use { "*" } for all types. The first extension in array will be used as default to create new files.
	 */
	FileDialogFilter(String[] extensions) {
		this.extensions = extensions;
	}

	/**
	 * Check whether a file matches extensions.
	 *
	 * @param filename The filename to check.
	 * @return The value will be true if the extension of the file equals one of extensions, or false if the extension of the file equals nothing.
	 */
	boolean meetExtensions(String filename) throws ArrayIndexOutOfBoundsException {
		if (extensions[0].equals(ALL)) return true;
		for (String e : extensions)
			if (filename.toLowerCase().endsWith(e)) return true;
		return false;
	}
}
