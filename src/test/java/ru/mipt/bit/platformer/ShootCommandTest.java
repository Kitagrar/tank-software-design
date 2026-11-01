package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.command.ShootCommand;
import ru.mipt.bit.platformer.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShootCommandTest {
    @Test
    void asksReceiverExactlyOnceAndRegistersItsConfiguredBullet() {
        int[] shots = {0};
        Bullet configured = new Bullet(new GridPoint2(2, 1), Direction.RIGHT, 17, 0.2f);
        Tank receiver = new Tank(new GridPoint2(1, 1)) {
            @Override
            public Bullet shoot() {
                shots[0]++;
                return configured;
            }
        };
        GameLevel level = new GameLevel(5, 3, new GridPoint2(0, 0), List.of()) {
            @Override
            public boolean containsTank(Tank tank) {
                return tank == receiver;
            }
        };
        ShootCommand command = new ShootCommand(receiver, level);
        assertEquals(0, shots[0]);
        command.execute();
        assertEquals(1, shots[0]);
        assertEquals(List.of(configured), level.getBullets());
        assertEquals(17, level.getBullets().get(0).getDamage());
    }

    @Test
    void constructionHasNoEffectAndExecutionRegistersReceiversShot() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(1, 1), List.of());
        Tank tank = level.getPlayerTank();
        ShootCommand command = new ShootCommand(tank, level);
        assertTrue(level.getBullets().isEmpty());
        command.execute();
        assertEquals(1, level.getBullets().size());
        Bullet bullet = level.getBullets().get(0);
        assertEquals(new GridPoint2(2, 1), bullet.getCoordinates());
        assertEquals(tank.getDirection(), bullet.getDirection());
        assertEquals(Bullet.DEFAULT_DAMAGE, bullet.getDamage());
    }

    @Test
    void staleCommandCannotShootAfterDeathOrRemoval() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(1, 1), List.of());
        Tank tank = level.getPlayerTank();
        ShootCommand command = new ShootCommand(tank, level);
        tank.takeDamage(tank.getHealth());
        command.execute();
        assertTrue(level.getBullets().isEmpty());
        level.removeTank(tank);
        command.execute();
        assertTrue(level.getBullets().isEmpty());
        new ShootCommand(new Tank(new GridPoint2(2, 2)), level).execute();
        assertTrue(level.getBullets().isEmpty());
    }

    @Test
    void commandRechecksStationaryStateAtExecution() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(1, 1), List.of());
        ShootCommand command = new ShootCommand(level.getPlayerTank(), level);
        assertTrue(level.getPlayerTank().tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        command.execute();
        assertTrue(level.getBullets().isEmpty());
    }
}
