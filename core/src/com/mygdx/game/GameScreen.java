package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class GameScreen implements Screen {

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Body boxBody;
    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        Vector2 gravity = new Vector2(0, -9.8f);
        world = new World(gravity, true);
        debugRenderer = new Box2DDebugRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() / 100f, Gdx.graphics.getHeight() / 100f);

        createGround();
        createBox();
        shapeRenderer = new ShapeRenderer();
    }

    private void createGround() {
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(3, 2));
        Body groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(1f, 0.1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        groundBody.createFixture(fixtureDef);

        createRamp(groundBody, -2.5f);
        createRamp(groundBody, 2.5f);

        groundBox.dispose();
    }

    private void createRamp(Body groundBody, float xOffset) {
        float slopeAngle = 45f;
        float slopeLength = 1.5f;
        float slopeHeight = (float) (slopeLength * Math.tan(Math.toRadians(slopeAngle)));

        Vector2[] vertices = new Vector2[3];

        // Left ramp (negative xOffset)
        if (xOffset < 0) {
            vertices[0] = new Vector2(xOffset, 0.1f);
            vertices[1] = new Vector2(xOffset, 0.1f + slopeHeight);
            vertices[2] = new Vector2(xOffset + slopeLength, 0.1f);
        }
        // Right ramp (positive xOffset)
        else {
            vertices[0] = new Vector2(xOffset, 0.1f);
            vertices[1] = new Vector2(xOffset - slopeLength, 0.1f);
            vertices[2] = new Vector2(xOffset, 0.1f + slopeHeight);
        }

        PolygonShape slopeShape = new PolygonShape();
        slopeShape.set(vertices);
        groundBody.createFixture(slopeShape, 0);

        slopeShape.dispose();
    }

    private void createBox() {
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = BodyType.DynamicBody;
        boxBodyDef.position.set(new Vector2(1, 3)); // Set the initial position of the box
        boxBody = world.createBody(boxBodyDef);

        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(0.1f, 0.1f); // Create a box shape
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = boxShape;
        boxBody.createFixture(fixtureDef);
        boxShape.dispose();
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Step the physics simulation
        world.step(1 / 60f, 6, 2);

        // Update camera
        camera.update();

        // Render debug
        debugRenderer.render(world, camera.combined);

        handleInput();

    }

    private void handleInput() {
        Vector2 velocity = boxBody.getLinearVelocity();
        Vector2 desiredVelocity = new Vector2(0, velocity.y);
        Vector2 velocityChange = new Vector2();
        Vector2 impulse = new Vector2();
        Vector2 weight = new Vector2(0, 0);
        Vector2 normal = getGroundNormal("left");
        
        weight.x = boxBody.getMass() * world.getGravity().y * normal.x;
        weight.y = boxBody.getMass() * world.getGravity().y * normal.y;
  
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            desiredVelocity.x = -1f;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            desiredVelocity.x = 1f;
        }
        else if (!Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            desiredVelocity.x = weight.x;
            desiredVelocity.y = weight.y;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            desiredVelocity.x = 0;
            desiredVelocity.y = 5f;
        }

        //boxBody.setTransform(boxBody.getPosition(), (float) Math.toRadians(-getGroundAngle(getGroundNormal("left")) ));
        velocityChange = desiredVelocity.sub(velocity);
        impulse = velocityChange.scl(boxBody.getMass());
        boxBody.applyLinearImpulse(impulse, boxBody.getWorldCenter(), true);
        Gdx.app.log("Velocity", velocity.toString());     

        
    }

    private double getGroundAngle(Vector2 normal) {
        double angleRad = Math.atan2(normal.x, normal.y);
        return angleRad * MathUtils.radiansToDegrees;
    }

    private boolean isBoxOnSlope(String direction) {
        Vector2 normal = getGroundNormal(direction);

        // Define a threshold angle to determine if the surface is a slope
        float slopeThreshold = 0.1f;

        // Calculate the angle between the normal and the vertical axis
        float angle = Math.abs(MathUtils.atan2(normal.x, normal.y));

        // Compare the angle with the threshold to determine if it's a slope
        return angle > slopeThreshold;
    }

    private Vector2 getGroundNormal(String direction) {
        // Half-width of the box
        float halfWidth = 0.1f;

        // Get the position of the box
        Vector2 boxCenter = boxBody.getPosition();
        Vector2 bottomVertex = new Vector2();

        // Calculate the bottom vertex of the box based on the direction
        if (direction.equals("left")) {
            bottomVertex = new Vector2(boxCenter).sub(halfWidth, halfWidth);
        } else if (direction.equals("right")) {
            bottomVertex = new Vector2(boxCenter).add(halfWidth, -halfWidth);
        }

        // Variable to store the normal of the left side
        final Vector2 leftNormal = new Vector2();

        Vector2 endPosition = new Vector2(boxCenter).mulAdd(bottomVertex.cpy().sub(boxCenter), 2.0f);

        // Create a callback that sets the normal when a ray hits the ground
        RayCastCallback leftCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                leftNormal.set(normal);
                return 0;
            }
        };

        // Perform a raycast from boxCenter to the extended end position
        world.rayCast(leftCallback, boxCenter, endPosition);

        drawRay(boxCenter, endPosition);

        return leftNormal;
    }

    private void drawRay(Vector2 starPoint, Vector2 endPoint) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(starPoint, endPoint);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
    }

}
