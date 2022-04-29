package sk.project.spec;

import java.io.Serializable;

public class SimpleIndexData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String file;
	private int offset;

	public SimpleIndexData(String file, int offset) {
		this.file = file;
		this.offset = offset;
	}

	public String getFile() {
		return file;
	}

	public int getOffset() {
		return offset;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
