import React from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  Avatar,
  Grid,
  Box,
  Link,
} from '@mui/material';
import AppBar from '../../component/AppBar';
import { usePersonalInfo } from '../../context/PersonalInfoContext';

const ProfilePage = () => {
  const { personalInfo } = usePersonalInfo();

  if (!personalInfo) {
    return <Typography variant="h6">Loading profile...</Typography>;
  }

  const {
    firstName,
    lastName,
    email,
    contactNumber,
    profilePictureUrl,
  } = personalInfo;

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar />

      <Box component="main" sx={{ flexGrow: 1, p: 4 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          My Profile
        </Typography>

        <Card sx={{ p: 3 }}>
          <Grid container spacing={4}>
            {/* Profile Picture */}
            <Grid item xs={12} md={4} textAlign="center">
              <Box>
                <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                  PROFILE PICTURE
                </Typography>
                <Avatar
                  src={profilePictureUrl || '/default-avatar.png'}
                  alt="Profile"
                  sx={{ width: 150, height: 150, mx: 'auto' }}
                />
              </Box>
            </Grid>

            {/* Personal Info */}
            <Grid item xs={12} md={8}>
              <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                PERSONAL INFORMATION
              </Typography>

              <Grid container spacing={2} direction="column">
                <Grid item container spacing={1}>
                  <Grid item xs={4}>
                    <Typography fontWeight="medium">First Name:</Typography>
                  </Grid>
                  <Grid item xs={8} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Typography>{firstName}</Typography>
                  </Grid>
                </Grid>

                <Grid item container spacing={1}>
                  <Grid item xs={4}>
                    <Typography fontWeight="medium">Last Name:</Typography>
                  </Grid>
                  <Grid item xs={8} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Typography>{lastName}</Typography>
                  </Grid>
                </Grid>

                <Grid item container spacing={1}>
                  <Grid item xs={4}>
                    <Typography fontWeight="medium">Email:</Typography>
                  </Grid>
                  <Grid item xs={8} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Link href={`mailto:${email}`} underline="hover">
                      {email}
                    </Link>
                  </Grid>
                </Grid>

                <Grid item container spacing={1}>
                  <Grid item xs={4}>
                    <Typography fontWeight="medium">Phone no:</Typography>
                  </Grid>
                  <Grid item xs={8} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <Typography>{contactNumber || '-'}</Typography>
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </Card>
      </Box>
    </Box>
  );
};

export default ProfilePage;
