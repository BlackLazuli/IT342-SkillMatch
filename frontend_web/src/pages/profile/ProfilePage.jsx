import React, { useState, useEffect } from 'react';
import {
  Typography,
  Card,
  Avatar,
  Grid,
  Box,
  Link,
  Button,
  Modal,
  TextField,
  Stack,
} from '@mui/material';
import AppBar from '../../component/AppBar';
import { usePersonalInfo } from '../../context/PersonalInfoContext';
import axios from 'axios';
import { GoogleMap, LoadScript, Marker, InfoWindow } from '@react-google-maps/api';

const ProfilePage = () => {
  const { personalInfo } = usePersonalInfo();
  const [openModal, setOpenModal] = useState(false);
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [existingAddress, setExistingAddress] = useState(null);
  const token = localStorage.getItem("token");

  if (!personalInfo) {
    return <Typography variant="h6">Loading profile...</Typography>;
  }

  const {
    firstName,
    lastName,
    email,
    contactNumber,
    profilePictureUrl,
    userId,
  } = personalInfo;

  useEffect(() => {
    const fetchAddress = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/locations/${userId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (response.data) {
          setExistingAddress(response.data.address);
          setLatitude(Number(response.data.latitude));
          setLongitude(Number(response.data.longitude));
        }
      } catch (error) {
        console.error("Error fetching address:", error);
      }
    };

    fetchAddress();
  }, [userId, token]);

  const handleOpenModal = () => setOpenModal(true);
  const handleCloseModal = () => setOpenModal(false);

  const handleAddressSubmit = async () => {
    if (!token) {
      alert("No authentication token found. Please log in again.");
      return;
    }

    if (!address) {
      alert("Please enter an address.");
      return;
    }

    try {
      const { lat, lng } = await getCoordinatesFromAddress(address);

      if (!lat || !lng) {
        alert("Unable to get coordinates for the address.");
        return;
      }

      const response = await axios.post(
        `http://localhost:8080/api/locations/${userId}`,
        { address, latitude: lat, longitude: lng },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      console.log("Address added:", response.data);
      alert("Address added successfully!");

      setLatitude(Number(lat));
      setLongitude(Number(lng));
      setExistingAddress(address);

      handleCloseModal();
    } catch (error) {
      console.error("Error adding address:", error);
      alert("Error adding address.");
    }
  };

  const getCoordinatesFromAddress = async (address) => {
    const apiKey = "AIzaSyC5Bgywlpo6HUd7ZV-8klLuaLeIBSjXbaE";
    try {
      const response = await axios.get(
        `https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(address)}&key=${apiKey}`
      );

      if (response.data.results.length > 0) {
        const { lat, lng } = response.data.results[0].geometry.location;
        return { lat, lng };
      } else {
        return { lat: '', lng: '' };
      }
    } catch (error) {
      console.error("Error fetching coordinates:", error);
      return { lat: '', lng: '' };
    }
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar />

      <Box component="main" sx={{ flexGrow: 1, p: 4 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          My Profile
        </Typography>

        <Card sx={{ p: 4 }}>
          <Grid container spacing={4}>
            <Grid item xs={12} md={4} textAlign="center">
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                PROFILE PICTURE
              </Typography>
              <Avatar
                src={profilePictureUrl || '/default-avatar.png'}
                alt="Profile"
                sx={{ width: 150, height: 150, mx: 'auto' }}
              />
            </Grid>

            <Grid item xs={12} md={8}>
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                PERSONAL INFORMATION
              </Typography>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex' }}>
                  <Typography fontWeight="medium" sx={{ width: 180 }}>
                    First Name:
                  </Typography>
                  <Typography>{firstName}</Typography>
                </Box>
                <Box sx={{ display: 'flex' }}>
                  <Typography fontWeight="medium" sx={{ width: 180 }}>
                    Last Name:
                  </Typography>
                  <Typography>{lastName}</Typography>
                </Box>
                <Box sx={{ display: 'flex' }}>
                  <Typography fontWeight="medium" sx={{ width: 180 }}>
                    Email:
                  </Typography>
                  <Link href={`mailto:${email}`} underline="hover">
                    {email}
                  </Link>
                </Box>
                <Box sx={{ display: 'flex' }}>
                  <Typography fontWeight="medium" sx={{ width: 180 }}>
                    Phone no:
                  </Typography>
                  <Typography>{contactNumber || '-'}</Typography>
                </Box>
              </Box>

              {!existingAddress && (
                <Button
                  variant="contained"
                  color="primary"
                  sx={{ mt: 3 }}
                  onClick={handleOpenModal}
                >
                  Add Address
                </Button>
              )}
            </Grid>
          </Grid>
        </Card>

        {/* My Location Section */}
        <Box sx={{ mt: 6 }}>
          <Typography variant="h4" fontWeight="bold" gutterBottom>
            My Location
          </Typography>

          <Card sx={{ p: 3 }}>
            {existingAddress ? (
              <>
                <Typography variant="h6" gutterBottom>
                  Address:
                </Typography>
                <Typography>{existingAddress}</Typography>

                {!isNaN(latitude) && !isNaN(longitude) ? (
                  <Box sx={{ width: '100%', height: 500, mt: 3 }}>
                    <LoadScript googleMapsApiKey="AIzaSyC5Bgywlpo6HUd7ZV-8klLuaLeIBSjXbaE">
                      <GoogleMap
                        center={{ lat: Number(latitude), lng: Number(longitude) }}
                        zoom={14}
                        mapContainerStyle={{ width: '100%', height: '100%' }}
                      >
                        <Marker position={{ lat: Number(latitude), lng: Number(longitude) }} />
                        <InfoWindow position={{ lat: Number(latitude), lng: Number(longitude) }}>
                          <Typography>{existingAddress}</Typography>
                        </InfoWindow>
                      </GoogleMap>
                    </LoadScript>
                  </Box>
                ) : (
                  <Typography color="error" sx={{ mt: 2 }}>
                    Invalid map coordinates.
                  </Typography>
                )}
              </>
            ) : (
              <Typography>No location available. Please add your address.</Typography>
            )}
          </Card>
        </Box>

        {/* Address Modal */}
        <Modal
          open={openModal}
          onClose={handleCloseModal}
          aria-labelledby="modal-title"
          aria-describedby="modal-description"
        >
          <Box
            sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              bgcolor: 'background.paper',
              boxShadow: 24,
              p: 4,
              borderRadius: 2,
              width: 400,
            }}
          >
            <Typography id="modal-title" variant="h6" component="h2">
              Add Your Address
            </Typography>
            <Stack spacing={2} sx={{ mt: 2 }}>
              <TextField
                label="Address"
                fullWidth
                variant="outlined"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />
            </Stack>
            <Box sx={{ mt: 3, textAlign: 'right' }}>
              <Button onClick={handleCloseModal} color="secondary" sx={{ mr: 2 }}>
                Cancel
              </Button>
              <Button onClick={handleAddressSubmit} variant="contained" color="primary">
                Save Address
              </Button>
            </Box>
          </Box>
        </Modal>
      </Box>
    </Box>
  );
};

export default ProfilePage;
