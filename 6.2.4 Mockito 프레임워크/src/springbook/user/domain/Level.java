package springbook.user.domain;

//����� ������ �̴�
//�̷��� ������� Level �̴��� ���ο��� DB�� ������ intŸ���� ���� ���� ������,
// �����δ� Level Ÿ���� ������Ʈ �̱� ������ �����ϰ� ����� �� �ִ�.
public enum Level {
	GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER);

	private final int value;
	private final Level next;

	public Level getNext() {
		return next;
	}

	Level(int value, Level next) {
		// DB�� ������ ���� �־��� �����ڸ� ����� �д�.
		this.value = value;
		this.next = next;
	}

	public int initValue() {
		return value;
	}
	public Level nextLevel(){
		return this.next;
	}
	
	public static Level valueOf(int value) {
		switch (value) {
		case 1:
			return BASIC;
		case 2:
			return SILVER;
		case 3:
			return GOLD;

		default:
			throw new AssertionError("Unknown value:" + value);

		}
	}
}
