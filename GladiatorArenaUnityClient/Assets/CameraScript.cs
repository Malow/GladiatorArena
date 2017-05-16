using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CameraScript : MonoBehaviour {

    private const float CAMERA_ROTATION_SPEED = 3.0f;
    private const float CAMERA_MOVESPEED_SPEED = 0.05f;
    private float scrollSpeed = 0.25f;

    private float xMin = 0;
    private float xMax = 100;
    private float yMin = 3;
    private float yMax = 20;
    private float zMin = 0;
    private float zMax = 100;
    private Vector3 desiredPostion;

    void Start() {
        desiredPostion = transform.position;
    }

    void Update()
    {
        float speed = scrollSpeed * Time.deltaTime;
        if (SystemInfo.deviceType == DeviceType.Desktop)
        {
            float x = 0, y = 0, z = 0;

            if (Input.GetKey(KeyCode.A) || Input.GetKey(KeyCode.LeftArrow))
            {
                x -= speed;
            }
            if (Input.GetKey(KeyCode.D) || Input.GetKey(KeyCode.RightArrow))
            {
                x += speed;
            }
            if (Input.GetKey(KeyCode.S) || Input.GetKey(KeyCode.DownArrow))
            {
                z -= speed;
            }
            if (Input.GetKey(KeyCode.W) || Input.GetKey(KeyCode.UpArrow))
            {
                z += speed;
            }

            y -= Input.GetAxis("Mouse ScrollWheel") * speed * 20;

            float desktopMultiplier = 100.0f;
            x *= desktopMultiplier * transform.position.y * 0.1f;
            y *= desktopMultiplier;
            z *= desktopMultiplier * transform.position.y * 0.1f;
            desiredPostion = (new Vector3(x, y, z) + desiredPostion);
        }
        else
        {
            if (Input.touchCount > 0 && Input.GetTouch(0).phase == TouchPhase.Moved)
            {
                Vector2 touchDeltaPosition = Input.GetTouch(0).deltaPosition;
                transform.Translate(-touchDeltaPosition.x * speed, 0, -touchDeltaPosition.y * speed);
                transform.position = new Vector3(transform.position.x, 10.0f, transform.position.z);
                desiredPostion = transform.position;
            }
        }

        desiredPostion.x = Mathf.Clamp(desiredPostion.x, xMin, xMax);
        desiredPostion.y = Mathf.Clamp(desiredPostion.y, yMin, yMax);
        desiredPostion.z = Mathf.Clamp(desiredPostion.z, zMin, zMax);
        transform.position = Vector3.Lerp(transform.position, desiredPostion, CAMERA_MOVESPEED_SPEED);
    }    
}
